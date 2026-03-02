package org.shadow.kdrainpluginapi

class PluginConfig(private val values: Map<String, String>) {

    fun int(key: String): Int =
        values[key]?.toIntOrNull() ?: 0

    fun text(key: String): String =
        values[key] ?: ""

    fun bool(key: String): Boolean =
        values[key]?.toBooleanStrictOrNull() ?: false

    fun decimal(key: String): Double =
        values[key]?.toDoubleOrNull() ?: 0.0

    fun has(key: String): Boolean = values.containsKey(key)

    fun toMap(): Map<String, String> = values.toMap()

    companion object {
        fun fromMap(map: Map<String, String>): PluginConfig = PluginConfig(map)

        fun defaults(fields: List<ConfigField>): PluginConfig {
            val map = mutableMapOf<String, String>()
            for (field in fields) {
                map[field.key] = when (field) {
                    is ConfigField.Int -> field.defaultValue.toString()
                    is ConfigField.Text -> field.defaultValue
                    is ConfigField.Bool -> field.defaultValue.toString()
                    is ConfigField.Decimal -> field.defaultValue.toString()
                    is ConfigField.Select -> field.defaultValue
                }
            }
            return PluginConfig(map)
        }
    }
}
