package org.shadow.project.plugin

import org.shadow.kdrainpluginapi.KDrainPlugin
import java.io.Closeable
import java.io.File
import java.net.URLClassLoader

data class PluginInfo(
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val pluginClass: Class<out KDrainPlugin>,
    val jarFile: File,
    val classLoader: URLClassLoader,
    val folderName: String? = null
) : Closeable {
    val id: String = if (folderName != null) "$folderName:$name:$version" else "$name:$version"
    val displayName: String = if (folderName != null) "$name ($folderName)" else name

    fun createInstance(): KDrainPlugin? =
        try {
            pluginClass.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            System.err.println("[PluginInfo] Failed to instantiate $name: ${e.message}")
            null
        }

    override fun close() {
        classLoader.close()
    }
}
