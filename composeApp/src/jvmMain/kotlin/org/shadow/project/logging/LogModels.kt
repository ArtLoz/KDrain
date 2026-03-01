package org.shadow.project.logging

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

enum class LogType(val displayName: String, val color: Color) {
    CLIENT_SERVER("C -> S", Color(0xFF4CAF50)),
    SERVER_CLIENT("S -> C", Color(0xFF2196F3)),
    ACTION("ACTION", Color(0xFFFF9800)),
    PLUGIN("PLUGIN", Color(0xFFCE93D8)),
    INFO("INFO", Color(0xFF9E9E9E)),
    ERROR("ERROR", Color(0xFFF44336))
}

data class LogEntry(
    val id: Long,
    val type: LogType,
    val message: String,
    val timestamp: LocalDateTime
) {
    companion object {
        private val ID_COUNTER = AtomicLong(0)
        val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        fun nextId(): Long = ID_COUNTER.incrementAndGet()
    }

    val formattedTime: String = timestamp.format(TIME_FMT)
}
