package org.shadow.kdrainpluginapi

sealed class ConfigField(
    val key: String,
    val label: String,
    val description: String = ""
) {
    class Int(
        key: String, label: String, description: String = "",
        val defaultValue: kotlin.Int = 0
    ) : ConfigField(key, label, description)

    class Text(
        key: String, label: String, description: String = "",
        val defaultValue: String = ""
    ) : ConfigField(key, label, description)

    class Bool(
        key: String, label: String, description: String = "",
        val defaultValue: Boolean = false
    ) : ConfigField(key, label, description)

    class Decimal(
        key: String, label: String, description: String = "",
        val defaultValue: Double = 0.0
    ) : ConfigField(key, label, description)

    class Select(
        key: String, label: String, description: String = "",
        val options: List<String>,
        val defaultValue: String = ""
    ) : ConfigField(key, label, description)
}
