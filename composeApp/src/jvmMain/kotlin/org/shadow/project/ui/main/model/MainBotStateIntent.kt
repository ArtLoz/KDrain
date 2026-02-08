package org.shadow.project.ui.main.model

import com.l2bot.bridge.api.L2Bot
import com.l2bot.bridge.models.entities.L2Char
import com.l2bot.bridge.models.entities.L2User
import org.shadow.project.plugin.PluginInfo

data class MainBotStateIntent(
    val bots: List<L2Bot> = emptyList(),
    val selectedBot: L2Bot? = null,
    val charterInfo: L2User? = null,
    val plugins: List<PluginInfo> = emptyList(),
    val activePlugin: String? = null,
)
