package com.example.simplefarm

import com.kbridge.utils.ResourceHelper
import com.example.simplefarm.farm.FarmZone
import com.l2bot.bridge.api.L2Bot
import kotlinx.coroutines.delay

private const val PLUGIN_NAME = "SimpleFarm"

suspend fun scriptRun(bot: L2Bot) {
    val config = PluginConfig.load(MyPlugin::class.java, PLUGIN_NAME)

    val buffId = config.getInt("buffId", 4328)
    val soeId = config.getInt("soeId", 10650)
    val ssId = config.getInt("ssId", 2509)
    val petRezItemId = config.getInt("petRezItemId", 0)
    val escapeSkillId = config.getInt("escapeSkillId", 0)

    val zone = resolveFarmZone(config.getString("farmZone", ""))
        ?: throw IllegalStateException("farmZone not specified or unknown in config")

    val farm = FarmBot(
        bot = bot,
        zone = zone,
        pluginName = PLUGIN_NAME,
        buffId = buffId,
        soeId = soeId,
        ssId = ssId,
        petRezItemId = petRezItemId,
        escapeSkillId = escapeSkillId
    )

    bot.gps.loadBase(ResourceHelper.getDatabasePath())
    farm.log("init", "GPS loaded, town=${zone.town::class.simpleName}, zone=${zone.name}")

    // ---- Main loop ----
    var faceControlOn = false

    while (true) {
        if (!farm.isGameOpen()) {
            farm.log("main", "game window not found, waiting...")
            delay(5_000)
            continue
        }

        // ---- IN TOWN ----
        if (farm.isInTown()) {
            faceControlOn = false

            // 1. Rez pet if dead
            farm.rezPetIfNeeded()

            // 2. Check soulshots
            if (farm.isSsLow()) {
                farm.log("main", "SS count < 300 (${farm.ssCount()}), stopping")
                break
            }

            // 3. Buff character
            farm.rebuffIfNeeded()

            // 4. Buff pet
            farm.rebuffPetIfNeeded()

            // 5. Teleport to spot
            farm.teleportToSpot()
            continue
        }

        // ---- ON SPOT (not in town) ----

        // Character dead -> go home
        if (farm.isDead()) {
            farm.log("spot", "character is dead, going home")
            bot.setFaceControl(0, false)
            faceControlOn = false
            bot.goHome()
            delay(15_000)
            continue
        }

        // Buff ending -> fight off and go home
        if (farm.isBuffEnding()) {
            farm.log("spot", "buff ending, returning home")
            farm.safeGoHome()
            faceControlOn = false
            continue
        }

        // Pet dead -> fight off and go home
        if (farm.hasPet() && farm.isPetDead()) {
            farm.log("spot", "pet is dead, returning home")
            farm.safeGoHome()
            faceControlOn = false
            continue
        }

        // SS low -> fight off and go home
        if (farm.isSsLow()) {
            farm.log("spot", "SS low (${farm.ssCount()}), returning home")
            farm.safeGoHome()
            faceControlOn = false
            continue
        }

        // All good -> enable face control if not already
        if (!faceControlOn) {
            farm.log("spot", "loading zone and starting farm")
            farm.loadZone()
            bot.setFaceControl(0, true)
            faceControlOn = true
        }

        delay(3_000)
    }

    // Cleanup
    bot.setFaceControl(0, false)
    farm.log("main", "script finished")
}

/**
 * Resolves FarmZone by name from config.
 * Add concrete FarmZone data objects to kutils and map them here.
 */
private fun resolveFarmZone(name: String): FarmZone? {
    // TODO: map zone names to concrete FarmZone data objects once defined
    // Example:
    // "GludioGolems" -> GludioGolems
    // "KamaelSpiders" -> KamaelSpiders
    return null
}
