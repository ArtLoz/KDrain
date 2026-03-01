package com.example.simplefarm

import java.io.File
import java.nio.file.Files

/**
 * Simple JSON config reader for flat config files. No external dependencies.
 */
class PluginConfig(private val data: Map<String, Any>) {

    fun getString(key: String, default: String = ""): String =
        data[key]?.toString() ?: default

    fun getInt(key: String, default: Int = 0): Int =
        (data[key] as? Int) ?: data[key]?.toString()?.toIntOrNull() ?: default

    fun getLong(key: String, default: Long = 0L): Long =
        (data[key] as? Long) ?: data[key]?.toString()?.toLongOrNull() ?: default

    fun getDouble(key: String, default: Double = 0.0): Double =
        (data[key] as? Double) ?: data[key]?.toString()?.toDoubleOrNull() ?: default

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        (data[key] as? Boolean) ?: data[key]?.toString()?.toBooleanStrictOrNull() ?: default

    @Suppress("UNCHECKED_CAST")
    fun getStringList(key: String): List<String> =
        (data[key] as? List<String>) ?: emptyList()

    fun has(key: String): Boolean = data.containsKey(key)

    override fun toString(): String = data.toString()

    companion object {
        private val ENTRY_REGEX = Regex(""""(\w+)"\s*:\s*("([^"\\]*(?:\\.[^"\\]*)*)"|(-?\d+\.\d+)|(-?\d+)|(true|false))""")
        private val ARRAY_REGEX = Regex(""""(\w+)"\s*:\s*\[([^\]]*)]""")
        private val ARRAY_ITEM_REGEX = Regex(""""([^"\\]*(?:\\.[^"\\]*)*)"""")

        /**
         * Loads config from the same directory as the JAR file.
         * If not found, extracts default from JAR resources.
         */
        fun load(pluginClass: Class<*>, pluginName: String, resourcePath: String = "config.json"): PluginConfig {
            val jarLocation = File(pluginClass.protectionDomain.codeSource.location.toURI()).parentFile
            val configFile = File(jarLocation, "$pluginName.json")

            if (!configFile.exists()) {
                val stream = pluginClass.classLoader.getResourceAsStream(resourcePath)
                    ?: throw IllegalStateException("Default config not found in JAR: $resourcePath")
                stream.use { Files.copy(it, configFile.toPath()) }
                println("[$pluginName] Created default config: ${configFile.absolutePath}")
            }

            return parse(configFile.readText())
        }

        fun parse(json: String): PluginConfig {
            val map = mutableMapOf<String, Any>()

            // Parse arrays first
            for (match in ARRAY_REGEX.findAll(json)) {
                val key = match.groupValues[1]
                val content = match.groupValues[2]
                map[key] = ARRAY_ITEM_REGEX.findAll(content).map { it.groupValues[1] }.toList()
            }

            // Parse scalar values
            for (match in ENTRY_REGEX.findAll(json)) {
                val key = match.groupValues[1]
                if (key in map) continue // already parsed as array
                val strVal = match.groupValues[3]
                val doubleVal = match.groupValues[4]
                val intVal = match.groupValues[5]
                val boolVal = match.groupValues[6]
                map[key] = when {
                    strVal.isNotEmpty() || match.groupValues[2].startsWith("\"") -> strVal
                    doubleVal.isNotEmpty() -> doubleVal.toDouble()
                    intVal.isNotEmpty() -> intVal.toInt()
                    boolVal.isNotEmpty() -> boolVal.toBoolean()
                    else -> match.groupValues[2]
                }
            }
            return PluginConfig(map)
        }
    }
}
