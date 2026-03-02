package org.shadow.project.plugin

import java.util.UUID

data class StagedPlugin(
    val pluginInfo: PluginInfo,
    val configId: String = UUID.randomUUID().toString(),
    val configLabel: String = "",
    val configValues: Map<String, String> = emptyMap(),
    val isConfigurable: Boolean = false
) {
    val stageKey: String get() = "${pluginInfo.id}::$configId"
}
