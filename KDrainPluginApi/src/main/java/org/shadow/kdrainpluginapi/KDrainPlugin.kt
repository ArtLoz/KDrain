package org.shadow.kdrainpluginapi

import com.l2bot.bridge.api.L2Bot

interface KDrainPlugin {
    val name: String
    val version: String
    val author: String
    val description: String

    suspend fun onEnable(bot: L2Bot)

    suspend fun onDisable() {}
}