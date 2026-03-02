package com.example.simplefarm

import com.l2bot.bridge.api.L2Bot
import org.shadow.kdrainpluginapi.ConfigField
import org.shadow.kdrainpluginapi.ConfigurablePlugin
import org.shadow.kdrainpluginapi.PluginConfig

class MyPlugin : ConfigurablePlugin {
    override val name = "SimpleFarm"
    override val version = "1.1.0"
    override val author = "Shadow"
    override val description = "Simple farm bot plugin"
    override var onLog: ((tag: String, message: String) -> Unit)? = null

    override val configFields: List<ConfigField> = listOf(
        ConfigField.Select(
            key = "farmZone",
            label = "Farm Zone",
            description = "Select the farming zone",
            options = listOf(
                "OrenClFirst", "OrenClSecond", "OrenClThree", "OrenClFour",
                "Oren46to51One", "Oren46to51Second", "Oren46to51Three", "Oren46to51Four",
                "HvZone40to46"
            ),
            defaultValue = "OrenClFirst"
        ),
        ConfigField.Int(
            key = "buffId",
            label = "Buff ID",
            description = "ID of the buff to track",
            defaultValue = 1045
        ),
        ConfigField.Int(
            key = "soeId",
            label = "SoE Item ID",
            description = "Scroll of Escape item ID",
            defaultValue = 736
        ),
        ConfigField.Int(
            key = "ssId",
            label = "Soulshot ID",
            description = "Soulshot item ID (stops farming when < 300)",
            defaultValue = 1463
        ),
        ConfigField.Int(
            key = "petRezItemId",
            label = "Pet Rez Item ID",
            description = "Pet resurrection item ID (0 = disabled)",
            defaultValue = 737
        ),
        ConfigField.Int(
            key = "escapeSkillId",
            label = "Escape Skill ID",
            description = "Escape skill ID (0 = disabled)",
            defaultValue = 2099
        )
    )

    override suspend fun onEnable(bot: L2Bot, config: PluginConfig) {
        scriptRun(bot, config, ::log)
    }
}
