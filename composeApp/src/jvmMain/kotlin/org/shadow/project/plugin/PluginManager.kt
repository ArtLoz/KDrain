package org.shadow.project.plugin

import com.l2bot.bridge.api.L2Bot
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.shadow.kdrainpluginapi.KDrainPlugin
import org.shadow.project.logging.LogController
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLClassLoader
import java.util.jar.JarFile

data class PluginRunKey(val pluginId: String, val botCharName: String)

class PluginManager(private val logController: LogController) {

    private val pluginsDir = File(System.getProperty("user.dir"), "app/plugins")

    private val _plugins = MutableStateFlow<List<PluginInfo>>(emptyList())
    val plugins = _plugins.asStateFlow()

    private val _activePlugins = MutableStateFlow<Map<PluginRunKey, ActivePluginEntry>>(emptyMap())
    val activePlugins = _activePlugins.asStateFlow()

    /** Tracked classloaders — lifecycle managed here, not in PluginInfo */
    private val classLoaders = mutableListOf<URLClassLoader>()

    data class ActivePluginEntry(
        val pluginId: String,
        val bot: L2Bot,
        val pluginName: String,
        val job: Job,
        val instance: KDrainPlugin
    )

    private fun stackTraceToString(e: Throwable): String {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }

    fun loadPlugins() {
        if (!pluginsDir.exists() || !pluginsDir.isDirectory) {
            System.err.println("[PluginManager] Plugins directory not found: ${pluginsDir.absolutePath}")
            _plugins.value = emptyList()
            return
        }

        // Stop all active plugins before closing classloaders
        stopAll()

        // Close old classloaders safely (no active plugins reference them now)
        classLoaders.forEach { cl ->
            try { cl.close() } catch (_: Throwable) {}
        }
        classLoaders.clear()

        val loaded = mutableListOf<PluginInfo>()

        // JARs в корне app/plugins/
        pluginsDir.listFiles { file -> file.extension == "jar" }?.forEach { jar ->
            try {
                discoverPluginClasses(jar).forEach { loaded.add(it) }
                println("[PluginManager] Loaded: ${jar.name}")
            } catch (e: Throwable) {
                System.err.println("[PluginManager] Failed to load ${jar.name}: ${stackTraceToString(e)}")
            }
        }

        // JARs в подпапках app/plugins/<folder>/
        pluginsDir.listFiles { file -> file.isDirectory }?.forEach { dir ->
            dir.listFiles { file -> file.extension == "jar" }?.forEach { jar ->
                try {
                    discoverPluginClasses(jar, dir.name).forEach { loaded.add(it) }
                    println("[PluginManager] Loaded: ${dir.name}/${jar.name}")
                } catch (e: Throwable) {
                    System.err.println("[PluginManager] Failed to load ${dir.name}/${jar.name}: ${stackTraceToString(e)}")
                }
            }
        }

        _plugins.value = loaded
        println("[PluginManager] Total plugins loaded: ${loaded.size}")
    }

    @Suppress("UNCHECKED_CAST") // Safe: verified KDrainPlugin::class.java.isAssignableFrom(clazz)
    private fun discoverPluginClasses(jarFile: File, folderName: String? = null): List<PluginInfo> {
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
                                jarFile = jarFile,
                                classLoader = classLoader,
                                folderName = folderName
                            )
                        )
                    }
                } catch (e: ClassNotFoundException) {
                    // Expected — dependency not available
                } catch (e: NoClassDefFoundError) {
                    // Expected — transitive dependency missing
                } catch (e: Throwable) {
                    System.err.println("[PluginManager] Error loading class $className: ${stackTraceToString(e)}")
                }
            }
        }

        if (result.isEmpty()) {
            classLoader.close()
        } else {
            classLoaders.add(classLoader)
        }

        return result
    }

    fun runPlugin(plugin: PluginInfo, bot: L2Bot, scope: CoroutineScope) {
        val key = PluginRunKey(plugin.id, bot.charName)

        // Stop existing run of this plugin on this bot if any
        stopPlugin(plugin.id, bot)

        val instance = plugin.createInstance() ?: run {
            val msg = "[PluginManager] Failed to create instance of ${plugin.name}"
            System.err.println(msg)
            logController.logError(bot.charName, msg)
            return
        }

        // Use LAZY start: register entry BEFORE starting to avoid race condition
        val job = scope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
            try {
                println("[PluginManager] Running plugin: ${plugin.name} for bot: ${bot.charName}")
                instance.onLog = { tag, msg ->
                    logController.logPlugin(bot.charName, "[${plugin.name}][$tag] $msg")
                }
                instance.onEnable(bot)
            } catch (e: CancellationException) {
                println("[PluginManager] Plugin cancelled: ${plugin.name} for bot: ${bot.charName}")
                throw e
            } catch (e: Throwable) {
                val trace = stackTraceToString(e)
                System.err.println("[PluginManager] Plugin error ${plugin.name}: $trace")
                logController.logError(bot.charName, "[${plugin.name}] Error: ${e::class.simpleName}: ${e.message}")
            } finally {
                try { instance.onDisable() } catch (e: Throwable) {
                    val trace = stackTraceToString(e)
                    System.err.println("[PluginManager] Plugin onDisable error ${plugin.name}: $trace")
                    logController.logError(bot.charName, "[${plugin.name}] onDisable error: ${e::class.simpleName}: ${e.message}")
                }
                // Only remove if this entry is still ours (not replaced by a re-run)
                _activePlugins.update { map ->
                    if (map[key]?.instance === instance) map - key else map
                }
                println("[PluginManager] Plugin finished: ${plugin.name} for bot: ${bot.charName}")
            }
        }

        _activePlugins.update {
            it + (key to ActivePluginEntry(plugin.id, bot, plugin.name, job, instance))
        }
        job.start()
    }

    fun stopPlugin(pluginId: String, bot: L2Bot) {
        val key = PluginRunKey(pluginId, bot.charName)
        _activePlugins.update { map ->
            map[key]?.job?.cancel()
            map - key
        }
    }

    fun stopPluginOnAllBots(pluginId: String) {
        _activePlugins.update { map ->
            val toRemove = map.filter { it.key.pluginId == pluginId }
            toRemove.values.forEach { it.job.cancel() }
            map - toRemove.keys
        }
    }

    fun stopAll() {
        _activePlugins.update { map ->
            map.values.forEach { it.job.cancel() }
            emptyMap()
        }
    }

    fun isRunning(pluginId: String, bot: L2Bot): Boolean {
        val key = PluginRunKey(pluginId, bot.charName)
        val entry = _activePlugins.value[key] ?: return false
        return entry.job.isActive
    }
}
