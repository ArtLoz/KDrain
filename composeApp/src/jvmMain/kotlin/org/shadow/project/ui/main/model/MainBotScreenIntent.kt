package org.shadow.project.ui.main.model

import com.l2bot.bridge.api.L2Bot

sealed interface MainBotScreenIntent {
    data object LoadBots : MainBotScreenIntent
    data class SelectBot(val bot: L2Bot) : MainBotScreenIntent
    data object ToggleConnection : MainBotScreenIntent
}