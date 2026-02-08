package org.shadow.project.plugin

import com.l2bot.bridge.api.L2Bot
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.shadow.kdrainpluginapi.KDrainPlugin
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile

class PluginManager {

    private val pluginsDir = File("app/plugins")

    private val _plugins = MutableStateFlow<List<PluginInfo>>(emptyList())
    val plugins = _plugins.asStateFlow()

    private val _activePlugin = MutableStateFlow<String?>(null)
    val activePlugin = _activePlugin.asStateFlow()

    private var activeJob: Job? = null

    fun loadPlugins() {
        if (!pluginsDir.exists() || !pluginsDir.isDirectory) {
            println("[PluginManager] Plugins directory not found: ${pluginsDir.absolutePath}")
            _plugins.value = emptyList()
            return
        }

        val jarFiles = pluginsDir.listFiles { file -> file.extension == "jar" } ?: emptyArray()
        val loaded = mutableListOf<PluginInfo>()

        for (jar in jarFiles) {
            try {
                val plugins = discoverPlugins(jar)
                for (plugin in plugins) {
                    loaded.add(
                        PluginInfo(
                            name = "${jar.nameWithoutExtension}:${plugin::class.simpleName}",
                            instance = plugin,
                            jarFile = jar
                        )
                    )
                    println("[PluginManager] Loaded: ${jar.name} -> ${plugin::class.qualifiedName}")
                }
            } catch (e: Exception) {
                println("[PluginManager] Failed to load ${jar.name}: ${e.message}")
                e.printStackTrace()
            }
        }

        _plugins.value = loaded
        println("[PluginManager] Total plugins loaded: ${loaded.size}")
    }

    private fun discoverPlugins(jarFile: File): List<KDrainPlugin> {
        val classLoader = URLClassLoader(
            arrayOf(jarFile.toURI().toURL()),
            this::class.java.classLoader
        )
        val plugins = mutableListOf<KDrainPlugin>()

        JarFile(jarFile).use { jar ->
            val entries = jar.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.name.endsWith(".class") || entry.name.contains("$")) continue

                val className = entry.name
                    .removeSuffix(".class")
                    .replace('/', '.')

                try {
                    val clazz = classLoader.loadClass(className)
                    if (KDrainPlugin::class.java.isAssignableFrom(clazz) && !clazz.isInterface) {
                        val instance = clazz.getDeclaredConstructor().newInstance() as KDrainPlugin
                        plugins.add(instance)
                    }
                } catch (_: Exception) {
                    // Skip classes that can't be loaded
                }
            }
        }

        return plugins
    }

    fun runPlugin(plugin: PluginInfo, bot: L2Bot, scope: CoroutineScope) {
        stopActivePlugin()

        activeJob = scope.launch(Dispatchers.IO) {
            try {
                _activePlugin.value = plugin.name
                println("[PluginManager] Running plugin: ${plugin.name}")
                plugin.instance.onEnable(bot)
            } catch (e: CancellationException) {
                println("[PluginManager] Plugin cancelled: ${plugin.name}")
            } catch (e: Exception) {
                println("[PluginManager] Plugin error ${plugin.name}: ${e.message}")
                e.printStackTrace()
            } finally {
                activeJob = null
                _activePlugin.value = null
                println("[PluginManager] Plugin finished: ${plugin.name}")
            }
        }
    }

    fun stopActivePlugin() {
        activeJob?.cancel()
        activeJob = null
        _activePlugin.value = null
    }
}
