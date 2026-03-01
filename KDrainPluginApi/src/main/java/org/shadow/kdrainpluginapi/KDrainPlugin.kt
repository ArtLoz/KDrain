package org.shadow.kdrainpluginapi

import com.l2bot.bridge.api.L2Bot

interface KDrainPlugin {
    val name: String
    val version: String
    val author: String
    val description: String

    var onLog: ((tag: String, message: String) -> Unit)?

    fun log(tag: String, message: String) {
        onLog?.invoke(tag, message)
    }

    suspend fun onEnable(bot: L2Bot)

    suspend fun onDisable() {}
}