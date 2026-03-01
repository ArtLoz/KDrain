package com.kbridge.utils.captcha

private const val FONT_TAG = "<font color=\"LEVEL\">"
private const val FONT_CLOSE = "</font>"

fun solveCaptcha(html: String): Int? {
    val lastStart = html.lastIndexOf(FONT_TAG)
    if (lastStart == -1) return null

    val exprStart = lastStart + FONT_TAG.length
    val exprEnd = html.indexOf(FONT_CLOSE, exprStart)
    if (exprEnd == -1) return null

    val expression = html.substring(exprStart, exprEnd).trim()

    val parts = expression.split(" ")
    if (parts.size != 3) return null

    val a = parts[0].toIntOrNull() ?: return null
    val op = parts[1]
    val b = parts[2].toIntOrNull() ?: return null

    return when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> if (b != 0) a / b else null
        else -> null
    }
}
