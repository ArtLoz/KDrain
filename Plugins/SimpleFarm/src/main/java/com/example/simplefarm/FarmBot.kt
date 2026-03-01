package com.example.simplefarm

import com.kbridge.utils.*
import com.kbridge.utils.location.*
import com.kbridge.utils.location.farm.FarmZone
import com.l2bot.bridge.api.L2Bot
import kotlinx.coroutines.delay

private const val SOE_DELAY = 35_000L
private const val SS_THRESHOLD = 300

class FarmBot(
    val bot: L2Bot,
    val zone: FarmZone,
    private val pluginName: String,
    private val buffId: Int,
    private val soeId: Int,
    private val ssId: Int,
    private val petRezItemId: Int = 0,
    private val escapeSkillId: Int = 0
) {

    val town: TownLocation get() = zone.town

    fun log(tag: String, msg: String) = println("[$pluginName][$tag] $msg")

    // ---- Status ----

    suspend fun level() = bot.user().level
    suspend fun isDead() = bot.user().isDead
    suspend fun isInZone() = bot.user().inZone
    suspend fun isBuffEnding() = bot.user().isBuffEnding(buffId)
    suspend fun hasBuff() = bot.user().hasBuff(buffId)
    suspend fun isInTown() = bot.isInLocation(town)
    suspend fun isGameOpen() = bot.getGameWindowHandle() != 0L
    suspend fun ssCount() = bot.inventory().getItemCount(ssId)
    suspend fun isSsLow() = ssCount() < SS_THRESHOLD

    // ---- Pet ----

    suspend fun hasPet() = bot.petList().isNotEmpty()
    suspend fun isPetDead() = bot.petList().petDead()
    suspend fun isPetBuffEnding(id: Int = buffId) =
        bot.petList().firstOrNull()?.isBuffEnding(id) ?: false

    suspend fun hasPetBuff(id: Int = buffId) =
        bot.petList().firstOrNull()?.hasBuff(id) ?: false

    // ---- Pet Rez ----

    suspend fun rezPetIfNeeded() {
        if (petRezItemId == 0 || !hasPet()) return
        if (!isPetDead()) return
        log("rezPet", "pet is dead, rezzing")
        val pet = bot.petList().firstOrNull() ?: return
        bot.setTargetId(pet.oid.toLong())
        delay(500)
        bot.useItemAndDelay(petRezItemId, 5_000)
        // verify
        repeat(10) {
            if (!isPetDead()) return
            delay(1_000)
        }
        log("rezPet", "pet rez result: dead=${isPetDead()}")
    }

    // ---- Buff ----

    suspend fun rebuff() {
        val path = zone.buffDialogPath
        if (path.isEmpty()) return
        val npc = town.npcNewbie
        log("rebuff", "start (${town::class.simpleName})")
        var attempts = 0
        while (isBuffEnding() && attempts < 5) {
            bot.moveByKGpsToNpc(npc)
            bot.targetAndConfirm(npc.id)
            bot.openDialogAndConfirm()
            for (index in path) {
                bot.selectedDialogByIndex(index)
            }
            delay(1_000)
            attempts++
        }
        log("rebuff", "done, hasBuff=${hasBuff()}")
    }

    suspend fun rebuffIfNeeded() {
        if (isBuffEnding()) rebuff()
    }

    suspend fun rebuffPet() {
        val path = zone.petBuffDialogPath
        if (path.isEmpty() || !hasPet()) return
        log("rebuffPet", "start")
        var attempts = 0
        while (isPetBuffEnding() && attempts < 5) {
            val npc = town.npcNewbie
            bot.moveByKGpsToNpc(npc)
            bot.targetAndConfirm(npc.id)
            bot.openDialogAndConfirm()
            for (index in path) {
                bot.selectedDialogByIndex(index)
            }
            delay(1_000)
            attempts++
        }
        log("rebuffPet", "done, hasPetBuff=${hasPetBuff()}")
    }

    suspend fun rebuffPetIfNeeded() {
        if (hasPet() && isPetBuffEnding()) rebuffPet()
    }

    // ---- Teleport ----

    suspend fun teleportToSpot() {
        val gk = town.npcGatekeeper
        log("teleport", "moving to GK")
        bot.moveByKGpsToNpc(gk)
        bot.targetAndConfirm(gk.id)
        bot.openDialogAndConfirm()
        for (index in zone.teleportPath) {
            bot.selectedDialogByIndex(index)
        }
        delay(15_000)
        log("teleport", "navigating to spot via GPS: ${zone.spot.gpsPointName}")
        bot.gps.moveTo(zone.spot.gpsPointName)
    }

    // ---- Return home ----

    suspend fun waitFightOff(timeoutMs: Long = 30_000) {
        log("waitFightOff", "waiting for mobs to deaggro")
        val start = System.currentTimeMillis()
        while (bot.hasAgrMobs()) {
            if (System.currentTimeMillis() - start > timeoutMs) {
                log("waitFightOff", "timeout reached")
                break
            }
            delay(500)
        }
    }

    suspend fun goHome() {
        if (bot.inventory().hasItemById(soeId)) {
            log("goHome", "using SoE (id=$soeId)")
            bot.useItemAndDelay(soeId, SOE_DELAY)
        } else {
            log("goHome", "no SoE, using escape skill (id=$escapeSkillId)")
            bot.useSkill(escapeSkillId.toLong())
            delay(SOE_DELAY)
        }
    }

    suspend fun safeGoHome() {
        waitFightOff()
        bot.setFaceControl(0, false)
        goHome()
    }

    // ---- Zone loading ----

    suspend fun loadZone() {
        val cl = MyPlugin::class.java.classLoader
        val zonePath = ResourceHelper.extractPluginResource(cl, zone.zoneFile, pluginName)
        bot.loadZone(zonePath)
        val cfg = zone.configFile
        if (cfg != null) {
            val configPath = ResourceHelper.extractPluginResource(cl, cfg, pluginName)
            bot.loadConfig(configPath)
        }
    }
}
