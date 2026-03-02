package org.shadow.kdrainpluginapi

import com.l2bot.bridge.api.L2Bot

interface ConfigurablePlugin : KDrainPlugin {

    val configFields: List<ConfigField>

    suspend fun onEnable(bot: L2Bot, config: PluginConfig)

    override suspend fun onEnable(bot: L2Bot) {
        throw UnsupportedOperationException(
            "ConfigurablePlugin must be started with a config. Use onEnable(bot, config) instead."
        )
    }
}
