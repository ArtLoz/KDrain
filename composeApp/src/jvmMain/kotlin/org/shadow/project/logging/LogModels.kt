package org.shadow.project.logging

import androidx.compose.ui.graphics.Color
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class LogType(val displayName: String, val color: Color) {
    CLIENT_SERVER("C -> S", Color(0xFF4CAF50)),
    SERVER_CLIENT("S -> C", Color(0xFF2196F3)),
    ACTION("ACTION", Color(0xFFFF9800)),
    INFO("INFO", Color(0xFF9E9E9E)),
    ERROR("ERROR", Color(0xFFF44336))
}

data class LogEntry(
    val type: LogType,
    val message: String,
    val timestamp: LocalTime
) {
    val formattedTime: String = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
}
