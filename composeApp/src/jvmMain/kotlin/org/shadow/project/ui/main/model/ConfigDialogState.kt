package org.shadow.project.ui.main.model

import org.shadow.kdrainpluginapi.ConfigField
import org.shadow.project.plugin.PluginInfo
import org.shadow.project.plugin.StagedPlugin

data class ConfigDialogState(
    val pluginInfo: PluginInfo,
    val fields: List<ConfigField>,
    val values: Map<String, String>,
    val configLabel: String,
    val isEditing: Boolean = false,
    val existingConfigId: String? = null,
    val existingConfigs: List<StagedPlugin> = emptyList(),
    val runAfterAdd: Boolean = false
)
