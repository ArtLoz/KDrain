package org.shadow.project.plugin

import org.shadow.kdrainpluginapi.KDrainPlugin
import java.io.File

data class PluginInfo(
    val name: String,
    val instance: KDrainPlugin,
    val jarFile: File
)
