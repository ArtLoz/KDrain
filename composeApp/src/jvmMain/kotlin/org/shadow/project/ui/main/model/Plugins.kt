package org.shadow.project.ui.main.model

import org.shadow.project.plugin.PluginInfo

data class PluginsUi(
    val name: String,
    val active: Boolean,
    val pluginInfo: PluginInfo,
    val details :String?,
    val id: String
)
