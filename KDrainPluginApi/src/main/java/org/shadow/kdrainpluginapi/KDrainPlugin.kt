package org.shadow.kdrainpluginapi

import com.l2bot.bridge.api.L2Bot

interface KDrainPlugin {
    suspend fun onEnable(bot: L2Bot)
}