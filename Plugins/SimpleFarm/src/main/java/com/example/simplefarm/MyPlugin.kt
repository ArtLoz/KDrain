package com.example.simplefarm

import com.l2bot.bridge.api.L2Bot
import org.shadow.kdrainpluginapi.KDrainPlugin

class MyPlugin : KDrainPlugin {
    override val name = "SimpleFarm"
    override val version = "1.0.0"
    override val author = "Shadow"
    override val description = "Simple farm bot plugin"

    override suspend fun onEnable(bot: L2Bot) {
        scriptRun(bot)
    }
}
