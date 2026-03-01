package com.kbridge.utils
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * A utility object that provides helper methods for accessing and managing application resources.
 */
object ResourceHelper {

    /**
     * Provides the absolute file system path where the database is located.
     *
     * @return The path to the database file.
     */
    fun getDatabasePath(resourcePath: String = "db/gps.db3", targetName: String = "gps.db3"): String {
        val workDir = File(System.getProperty("user.dir"), "sdk_data")
        if (!workDir.exists()) workDir.mkdirs()
        val targetFile = File(workDir, targetName)
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("Resource not found: $resourcePath")

        stream.use { input ->
            Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        return targetFile.absolutePath
    }

    /**
     * Extracts a resource from a plugin JAR to disk and returns the absolute path.
     *
     * @param classLoader The plugin's classloader (e.g. this::class.java.classLoader)
     * @param resourcePath Path to the resource inside the JAR (e.g. "zones/GOLD_ZONE.zmap")
     * @param pluginName Unique plugin name, used as subdirectory
     * @return Absolute path to the extracted file
     */
    fun extractPluginResource(
        classLoader: ClassLoader,
        resourcePath: String,
        pluginName: String
    ): String {
        val pluginDir = File(System.getProperty("user.dir"), "app/plugins_data/$pluginName")
        if (!pluginDir.exists()) pluginDir.mkdirs()

        val fileName = resourcePath.substringAfterLast('/')
        val targetFile = File(pluginDir, fileName)

        val stream = classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("Plugin resource not found: $resourcePath")

        stream.use { input ->
            Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        return targetFile.absolutePath
    }
}