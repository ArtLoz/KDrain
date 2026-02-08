package com.example.scrydepatheoplug

import com.l2bot.bridge.api.L2Bot
import org.shadow.kdrainpluginapi.KDrainPlugin

class MyPlugin : KDrainPlugin {
    override suspend fun onEnable(bot: L2Bot) {
        scriptRun(bot)
    }
}