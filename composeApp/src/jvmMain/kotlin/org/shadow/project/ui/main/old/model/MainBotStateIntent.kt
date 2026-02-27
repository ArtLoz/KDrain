package org.shadow.project.ui.main.old.model

import com.l2bot.bridge.api.L2Bot
import com.l2bot.bridge.models.entities.L2User
import org.shadow.project.plugin.PluginInfo
import org.shadow.project.ui.main.model.PluginsUi

data class MainBotStateIntent(
    val bots: List<L2Bot> = emptyList(),
    val selectedBot: L2Bot? = null,
    val user: L2User? = null,
    val plugins: List<PluginInfo> = emptyList(),
    val searchPlugin: String = "",
    val stagedPlugins: List<PluginInfo> = emptyList(),
    val activePluginsUi: List<PluginsUi> = emptyList()
)
