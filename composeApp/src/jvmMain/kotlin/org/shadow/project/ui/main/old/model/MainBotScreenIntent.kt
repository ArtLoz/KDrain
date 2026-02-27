package org.shadow.project.ui.main.old.model

import com.l2bot.bridge.api.L2Bot
import org.shadow.project.plugin.PluginInfo
import org.shadow.project.ui.main.model.PluginsUi

sealed interface MainBotScreenIntent {
    data object LoadBots : MainBotScreenIntent
    data class SelectBot(val bot: L2Bot) : MainBotScreenIntent
    data object ToggleConnection : MainBotScreenIntent
    data object LoadPlugins : MainBotScreenIntent
    data class SearchPlugin(val query: String) : MainBotScreenIntent

    // Library -> Active list
    data class AddPluginToActive(val plugin: PluginInfo) : MainBotScreenIntent
    data class RunPluginFromLibrary(val plugin: PluginInfo) : MainBotScreenIntent

    // Active list actions
    data class TogglePluginStatus(val plugin: PluginsUi) : MainBotScreenIntent
    data class RemoveActivePlugin(val plugin: PluginsUi) : MainBotScreenIntent
    data object StopAllPlugins : MainBotScreenIntent
}
