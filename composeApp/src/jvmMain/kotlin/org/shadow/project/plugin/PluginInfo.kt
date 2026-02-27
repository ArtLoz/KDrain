package org.shadow.project.plugin

import org.shadow.kdrainpluginapi.KDrainPlugin
import java.io.File

data class PluginInfo(
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val pluginClass: Class<out KDrainPlugin>,
    val jarFile: File
) {
    val id: String = "$name:$version"

    fun createInstance(): KDrainPlugin =
        pluginClass.getDeclaredConstructor().newInstance()
}
