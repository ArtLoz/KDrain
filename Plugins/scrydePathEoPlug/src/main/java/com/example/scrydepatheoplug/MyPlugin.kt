package com.example.scrydepatheoplug

import com.l2bot.bridge.api.L2Bot
import org.shadow.kdrainpluginapi.KDrainPlugin

class MyPlugin : KDrainPlugin {
    override val name = "ScrydePathEo"
    override val version = "1.0.0"
    override val author = "Shadow"
    override val description = "Scryde path bot plugin"

    override suspend fun onEnable(bot: L2Bot) {
        scriptRun(bot)
    }
}