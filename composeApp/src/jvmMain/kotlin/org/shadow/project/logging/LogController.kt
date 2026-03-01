package org.shadow.project.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class LogController {

    companion object {
        private const val MAX_LOGS = 10_000
        private const val MAX_LOG_FILE_BYTES = 10L * 1024 * 1024 // 10 MB
    }

    /** Per-bot log flows — only the affected bot's list is copied on each log entry */
    private val botLogs = ConcurrentHashMap<String, MutableStateFlow<List<LogEntry>>>()

    private val _enabledTypes = MutableStateFlow(LogType.entries.toSet())
    val enabledTypes = _enabledTypes.asStateFlow()

    private val _saveEnabled = MutableStateFlow(false)
    val saveEnabled = _saveEnabled.asStateFlow()

    private val logsDir = File(System.getProperty("user.dir"), "app/logs")
    private val writeLock = Any()
    private val writers = HashMap<String, BufferedWriter>()
    private var logsDirCreated = false

    private fun botFlow(botName: String): MutableStateFlow<List<LogEntry>> =
        botLogs.getOrPut(botName) { MutableStateFlow(emptyList()) }

    fun logsFor(botName: String): StateFlow<List<LogEntry>> = botFlow(botName).asStateFlow()

    fun toggleSave() {
        val wasEnabled = _saveEnabled.value
        _saveEnabled.update { !it }
        if (wasEnabled) {
            synchronized(writeLock) {
                writers.values.forEach { try { it.close() } catch (_: Exception) {} }
                writers.clear()
            }
        }
    }

    fun log(botName: String, type: LogType, message: String) {
        val now = LocalDateTime.now()
        val entry = LogEntry(LogEntry.nextId(), type, message, now)
        botFlow(botName).update { current ->
            (current + entry).takeLast(MAX_LOGS)
        }
        if (_saveEnabled.value) {
            val line = "[${now.format(LogEntry.TIME_FMT)}] ${type.displayName} $message\n"
            synchronized(writeLock) {
                if (!logsDirCreated) {
                    logsDir.mkdirs()
                    logsDirCreated = true
                }
                val logFile = File(logsDir, "$botName.log")
                if (logFile.exists() && logFile.length() > MAX_LOG_FILE_BYTES) {
                    writers.remove(botName)?.close()
                    val rotated = File(logsDir, "$botName.1.log")
                    rotated.delete()
                    logFile.renameTo(rotated)
                }
                val writer = writers.getOrPut(botName) {
                    BufferedWriter(FileWriter(logFile, true))
                }
                writer.write(line)
                writer.flush()
            }
        }
    }

    fun logClientServer(botName: String, message: String) = log(botName, LogType.CLIENT_SERVER, message)
    fun logServerClient(botName: String, message: String) = log(botName, LogType.SERVER_CLIENT, message)
    fun logAction(botName: String, message: String) = log(botName, LogType.ACTION, message)
    fun logInfo(botName: String, message: String) = log(botName, LogType.INFO, message)
    fun logPlugin(botName: String, message: String) = log(botName, LogType.PLUGIN, message)
    fun logError(botName: String, message: String) = log(botName, LogType.ERROR, message)

    fun clear(botName: String) {
        botFlow(botName).update { emptyList() }
    }

    fun toggleType(type: LogType) {
        _enabledTypes.update { current ->
            if (current.contains(type)) current - type else current + type
        }
    }
}
