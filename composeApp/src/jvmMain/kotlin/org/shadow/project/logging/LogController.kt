package org.shadow.project.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime

class LogController {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs = _logs.asStateFlow()

    private val _enabledTypes = MutableStateFlow(LogType.entries.toSet())
    val enabledTypes = _enabledTypes.asStateFlow()

    fun log(type: LogType, message: String) {
        _logs.update { it + LogEntry(type, message, LocalTime.now()) }
    }

    fun logClientServer(message: String) = log(LogType.CLIENT_SERVER, message)
    fun logServerClient(message: String) = log(LogType.SERVER_CLIENT, message)
    fun logAction(message: String) = log(LogType.ACTION, message)
    fun logInfo(message: String) = log(LogType.INFO, message)
    fun logError(message: String) = log(LogType.ERROR, message)

    fun clear() {
        _logs.value = emptyList()
    }

    fun toggleType(type: LogType) {
        _enabledTypes.update { current ->
            if (current.contains(type)) current - type else current + type
        }
    }
}
