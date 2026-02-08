package org.shadow.project.ui.main.model

import com.l2bot.bridge.api.L2Bot
import org.shadow.project.plugin.PluginInfo

sealed interface MainBotScreenIntent {
    data object LoadBots : MainBotScreenIntent
    data class SelectBot(val bot: L2Bot) : MainBotScreenIntent
    data object ToggleConnection : MainBotScreenIntent
    data object RunTestScript : MainBotScreenIntent
    data object LoadPlugins : MainBotScreenIntent
    data class RunPlugin(val plugin: PluginInfo) : MainBotScreenIntent
    data object StopPlugin : MainBotScreenIntent
}
