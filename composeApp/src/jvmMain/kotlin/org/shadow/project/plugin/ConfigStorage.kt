package org.shadow.project.plugin

import java.io.File

object ConfigStorage {
    private val baseDir: File by lazy {
        val userDir = File(System.getProperty("user.dir"))
        val direct = File(userDir, "app/plugins/configs")
        val parent = File(userDir.parentFile, "app/plugins/configs")
        if (File(userDir, "app/plugins").exists()) direct
        else if (File(userDir.parentFile, "app/plugins").exists()) parent
        else direct
    }

    fun save(pluginId: String, configId: String, values: Map<String, String>) {
        val dir = File(baseDir, pluginId.replace(":", "_"))
        dir.mkdirs()
        val file = File(dir, "$configId.json")
        val json = buildString {
            appendLine("{")
            values.entries.forEachIndexed { i, (k, v) ->
                val comma = if (i < values.size - 1) "," else ""
                appendLine("""  "$k": "${v.replace("\\", "\\\\").replace("\"", "\\\"")}"$comma""")
            }
            appendLine("}")
        }
        file.writeText(json)
    }

    fun load(pluginId: String, configId: String): Map<String, String>? {
        val dir = File(baseDir, pluginId.replace(":", "_"))
        val file = File(dir, "$configId.json")
        if (!file.exists()) return null
        return parseSimpleJson(file.readText())
    }

    fun delete(pluginId: String, configId: String) {
        val dir = File(baseDir, pluginId.replace(":", "_"))
        File(dir, "$configId.json").delete()
    }

    private fun parseSimpleJson(json: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val regex = Regex(""""([^"\\]*(?:\\.[^"\\]*)*)"\s*:\s*"([^"\\]*(?:\\.[^"\\]*)*)"""")
        for (match in regex.findAll(json)) {
            map[match.groupValues[1]] = match.groupValues[2]
        }
        return map
    }
}
