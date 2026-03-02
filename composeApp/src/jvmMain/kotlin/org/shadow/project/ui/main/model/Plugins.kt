package org.shadow.project.ui.main.model

import androidx.compose.ui.graphics.Color
import org.shadow.project.plugin.PluginInfo
import org.shadow.project.plugin.StagedPlugin

data class BotRunInfo(
    val botCharName: String,
    val color: Color,
    val isRunning: Boolean
)

data class PluginsUi(
    val name: String,
    val active: Boolean,
    val pluginInfo: PluginInfo,
    val details: String?,
    val id: String,
    val folderName: String? = null,
    val runningBots: List<BotRunInfo> = emptyList(),
    val configLabel: String? = null,
    val configId: String? = null,
    val stagedPlugin: StagedPlugin? = null
)
