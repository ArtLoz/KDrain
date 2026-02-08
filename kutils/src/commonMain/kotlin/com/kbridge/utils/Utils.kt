package com.kbridge.utils

import com.kbridge.utils.location.TownLocation
import com.kbridge.utils.location.NpcInfo
import com.l2bot.bridge.api.L2Bot
import com.l2bot.bridge.models.entities.L2Pet
import com.l2bot.bridge.models.entities.L2User
import com.l2bot.bridge.models.interfaces.IL2Live
import com.l2bot.bridge.models.interfaces.inRange
import com.l2bot.bridge.models.item.L2Item
import com.l2bot.bridge.models.types.L2Status
import kotlinx.coroutines.delay

fun L2User.isCast(): Boolean {
    return this.castInfo?.id != 0
}

fun IL2Live.levelGte(level: Int): Boolean {
    return this.level >= level
}

fun IL2Live.levelLte(level: Int): Boolean {
    return this.level < level
}

fun IL2Live.levelEq(level: Int): Boolean {
    return this.level == level
}

fun List<L2Item>.hasItemById(id: Int): Boolean {
    return this.any { it.id == id }
}

fun List<L2Item>.isEquipped(id: Int): Boolean {
    return this.find { it.id == id }?.equipped == true
}

fun IL2Live.isBuffEnding(idBuff: Int): Boolean {
    return (this.buffs.find { it.id == idBuff }?.endTime ?: 0) < 50_000
}

fun IL2Live.hasBuff(idBuff: Int): Boolean {
    return this.buffs.any { it.id == idBuff }
}

fun List<L2Item>.getItemCount(id: Int): Int {
    return this.count { it.id == id }
}

fun List<L2Pet>.petDead(): Boolean {
    return this.isNotEmpty() && this.any { it.isDead }
}

suspend inline fun L2Bot.hasAgrMobs(): Boolean {
    val user = this.user()
    return this.npcList().any { npc ->
        npc.attackOid == user.oid && npc.inCombat && !npc.isDead
    }
}

suspend inline fun L2Bot.useItemAndDelay(idItem: Int, delay: Long) {
    if (this.useItem(id = idItem.toLong())) {
        delay(delay)
    }
}

suspend inline fun L2Bot.targetAndConfirm(id: Int) {
    val startTime = System.currentTimeMillis()
    var lastRetry = startTime
    this.setTargetId(id.toLong())
    while (user().target?.id != id) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - startTime > 5000) {
            break
        }
        if (currentTime - lastRetry > 1000) {
            this.setTargetId(id.toLong())
            lastRetry = currentTime
        }
        delay(100)
    }
}

suspend inline fun L2Bot.openDialogAndConfirm() {
    while (!this.dlgOpen()) {
        delay(500)
    }
}

suspend inline fun L2Bot.selectedDialogByIndex(id: Int, delay: Long = 500) {
    this.dlgSel(id)
    delay(delay)
}

suspend inline fun L2Bot.selectedDialogByText(text: String, delay: Long = 100) {
    this.dlgSel(text)
    delay(delay)
}

suspend inline fun L2Bot.isInLocation(x: Int, y: Int, z: Int, radius: Int): Boolean {
    return user().inRange(x, y, z, radius)
}

suspend inline fun L2Bot.isInLocation(townLocation: TownLocation): Boolean {
    return user().inRange(
        townLocation.centerPoint.location.x,
        townLocation.centerPoint.location.y,
        townLocation.centerPoint.location.z,
        townLocation.centerPoint.range
    )
}
suspend inline fun L2Bot.isRangeNpc(npc:NpcInfo, radius: Int): Boolean {
    return user().inRange(npc.location.x, npc.location.y, npc.location.z, radius)
}

suspend inline fun L2Bot.getQuestItemCount(id: Int): Long {
    return questInventory().filter { it.id == id }.sumOf { it.count }
}

suspend inline fun L2Bot.hasQuestItem(id: Int): Boolean {
    return questInventory().any { it.id == id }
}

suspend inline fun L2Bot.relog(charIndex: Int = -1): Boolean {
    if (charIndex > 6) return false

    while (user().inCombat) {
        delay(99)
    }
    if (restart()) {
        while (this.getStatus() == L2Status.lsOnline) {
            delay(999)
        }
    }
    delay(5000)
    val startSuccess = if (charIndex == -1) {
        this.gameStart()
    } else {
        this.gameStart(charIndex)
    }
    if (startSuccess) {
        while (this.getStatus() != L2Status.lsOnline) {
            delay(999)
        }
    }
    delay(5000)
    return true
}

suspend inline fun L2Bot.moveByGPStoNpc(npc: NpcInfo){
    this.moveGpsPoint(npc.gpsPointName)
}

suspend inline fun L2Bot.moveByKGpsToNpc(npc: NpcInfo){
    this.gps.moveTo(npc.gpsPointName)
}