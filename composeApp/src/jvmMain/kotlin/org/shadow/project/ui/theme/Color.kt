package org.shadow.project.ui.theme

import androidx.compose.ui.graphics.Color


// Основные фоновые цвета
val BotBackground = Color(0xFF0E1016) // Самый темный фон окна
val BotSurface = Color(0xFF161922)    // Цвет карточек/панелей
val BotSurfaceVariant = Color(0xFF1F232F) // Немного светлее для хедеров или инпутов

// Акцентные цвета (Status indicators)
val AccentGreen = Color(0xFF2ECC71)   // Farming / Running / Info
val AccentRed = Color(0xFFE74C3C)     // Error / Disconnected / HP Bar
val AccentBlue = Color(0xFF3498DB)    // MP Bar / Active Count
val AccentYellow = Color(0xFFF1C40F)  // Warning / Standby

// Текстовые цвета
val TextPrimary = Color(0xFFECF0F1)   // Основной белый текст
val TextSecondary = Color(0xFF95A5A6) // Серый текст (ID, описания)
val TextLogInfo = Color(0xFF5DADE2)   // Голубой текст в логах [INFO]
val TextLogAct = Color(0xFF58D68D)    // Зеленый текст в логах [ACT]

// Bot color palette — 8 unique colors for bot identification
val BotColorPalette = listOf(
    Color(0xFF3498DB), // Blue
    Color(0xFF2ECC71), // Green
    Color(0xFFE67E22), // Orange
    Color(0xFF9B59B6), // Purple
    Color(0xFFE74C3C), // Red
    Color(0xFF1ABC9C), // Teal
    Color(0xFFF39C12), // Amber
    Color(0xFFE91E63), // Pink
)