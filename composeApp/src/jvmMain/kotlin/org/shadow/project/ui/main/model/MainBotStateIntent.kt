package org.shadow.project.ui.main.model

import androidx.compose.ui.graphics.Color
import com.l2bot.bridge.api.L2Bot
import com.l2bot.bridge.models.entities.L2User
import org.shadow.project.plugin.PluginInfo
import org.shadow.project.plugin.StagedPlugin

data class MainBotStateIntent(
    val bots: List<L2Bot> = emptyList(),
    val selectedBot: L2Bot? = null,
    val user: L2User? = null,
    val plugins: List<PluginInfo> = emptyList(),
    val searchPlugin: String = "",
    val stagedPlugins: List<StagedPlugin> = emptyList(),
    val activePluginsUi: List<PluginsUi> = emptyList(),
    val libraryPluginsUi: List<PluginsUi> = emptyList(),
    val botColorMap: Map<String, Color> = emptyMap(),
    val configDialogState: ConfigDialogState? = null
)
