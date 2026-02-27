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

    private val _activeBotPlugins = MutableStateFlow<Map<L2Bot, ActivePluginEntry>>(emptyMap())
    val activeBotPlugins = _activeBotPlugins.asStateFlow()

    data class ActivePluginEntry(
        val pluginName: String,
        val job: Job
    )

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
                val discovered = discoverPluginClasses(jar)
                for (pluginInfo in discovered) {
                    loaded.add(pluginInfo)
                    println("[PluginManager] Loaded: ${jar.name} -> ${pluginInfo.name} v${pluginInfo.version}")
                }
            } catch (e: Exception) {
                println("[PluginManager] Failed to load ${jar.name}: ${e.message}")
                e.printStackTrace()
            }
        }

        _plugins.value = loaded
        println("[PluginManager] Total plugins loaded: ${loaded.size}")
    }

    @Suppress("UNCHECKED_CAST")
    private fun discoverPluginClasses(jarFile: File): List<PluginInfo> {
        val classLoader = URLClassLoader(
            arrayOf(jarFile.toURI().toURL()),
            this::class.java.classLoader
        )
        val result = mutableListOf<PluginInfo>()

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
                        val pluginClass = clazz as Class<out KDrainPlugin>
                        val instance = pluginClass.getDeclaredConstructor().newInstance()

                        result.add(
                            PluginInfo(
                                name = instance.name,
                                version = instance.version,
                                author = instance.author,
                                description = instance.description,
                                pluginClass = pluginClass,
                                jarFile = jarFile
                            )
                        )
                    }
                } catch (_: Exception) {
                    // Skip classes that can't be loaded
                }
            }
        }

        return result
    }

    fun runPlugin(plugin: PluginInfo, bot: L2Bot, scope: CoroutineScope) {
        stopPlugin(bot)

        val instance = plugin.createInstance()

        val job = scope.launch(Dispatchers.IO) {
            try {
                println("[PluginManager] Running plugin: ${plugin.name} for bot: ${bot.charName}")
                instance.onEnable(bot)
            } catch (e: CancellationException) {
                println("[PluginManager] Plugin cancelled: ${plugin.name} for bot: ${bot.charName}")
            } catch (e: Exception) {
                println("[PluginManager] Plugin error ${plugin.name}: ${e.message}")
                e.printStackTrace()
            } finally {
                _activeBotPlugins.update { it - bot }
                println("[PluginManager] Plugin finished: ${plugin.name} for bot: ${bot.charName}")
            }
        }

        _activeBotPlugins.update { it + (bot to ActivePluginEntry(plugin.name, job)) }
    }

    fun stopPlugin(bot: L2Bot) {
        _activeBotPlugins.value[bot]?.job?.cancel()
        _activeBotPlugins.update { it - bot }
    }

    fun getActivePluginFor(bot: L2Bot): String? {
        return _activeBotPlugins.value[bot]?.pluginName
    }
}
