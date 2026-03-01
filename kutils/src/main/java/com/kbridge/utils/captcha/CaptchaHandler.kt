package com.kbridge.utils.captcha

import com.l2bot.bridge.api.L2Bot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CAPTCHA_NPC_HEADER = "19"
private const val ANSWER_OPCODE = 0x23

fun CoroutineScope.launchCaptchaHandler(bot: L2Bot, log: (String, String) -> Unit) = launch {
    bot.packetEvents.collect { event ->
        if (event.header != CAPTCHA_NPC_HEADER) return@collect

        val html = readStringFromHex(event.data) ?: return@collect
        val answer = solveCaptcha(html) ?: return@collect

        log("captcha", "solved: $answer")
        delay(5_000)

        val packet = buildPacketHex {
            writeC(ANSWER_OPCODE)
            writeS("00 $answer")
        }
        bot.sendToServer(packet)
        log("captcha", "answer sent")
    }
}

private fun readStringFromHex(hex: String): String? {
    val bytes = hexToBytes(hex) ?: return null
    if (bytes.size < 6) return null
    // Skip first 4 bytes (ReadD)
    var pos = 4
    val sb = StringBuilder()
    while (pos + 1 < bytes.size) {
        val lo = bytes[pos].toInt() and 0xFF
        val hi = bytes[pos + 1].toInt() and 0xFF
        val ch = (hi shl 8) or lo
        if (ch == 0) break
        sb.append(ch.toChar())
        pos += 2
    }
    return sb.toString()
}

private fun hexToBytes(hex: String): ByteArray? {
    val clean = hex.replace(" ", "")
    if (clean.length % 2 != 0) return null
    return ByteArray(clean.length / 2) { i ->
        clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
}

private fun buildPacketHex(block: PacketBuilder.() -> Unit): String {
    val builder = PacketBuilder()
    builder.block()
    return builder.toHex()
}

private class PacketBuilder {
    private val data = ByteArray(10240)
    private var pos = 0

    fun writeC(value: Int) {
        data[pos++] = value.toByte()
    }

    fun writeS(value: String) {
        for (ch in value) {
            data[pos++] = (ch.code and 0xFF).toByte()
            data[pos++] = (ch.code shr 8).toByte()
        }
        data[pos++] = 0
        data[pos++] = 0
    }

    fun toHex(): String = data.take(pos).joinToString("") {
        "%02X".format(it)
    }
}
