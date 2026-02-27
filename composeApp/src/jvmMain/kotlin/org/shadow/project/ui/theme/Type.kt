package org.shadow.project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BotTypography = Typography(
    // Основной текст (названия ботов, скриптов)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    // Текст описаний и статусов
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary
    ),
    // ЗАГОЛОВКИ СЕКЦИЙ (SUB-BOTS, LIBRARY - капсом)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp, // Разрядка букв как на макете
        color = TextSecondary
    ),
    // Для логов (System Logs)
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace, // Важно: моноширинный шрифт
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = TextSecondary
    )
)