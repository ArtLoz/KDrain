package com.example.scrydepatheoplug

import com.kbridge.utils.*
import com.kbridge.utils.location.*
import com.l2bot.bridge.api.L2Bot
import kotlinx.coroutines.delay

// ==================== Paths & Zones ====================

private const val PLUGIN_NAME = "scrydePathEoPlug"
private val PLUGIN_CL = MyPlugin::class.java.classLoader

private val GPS_DB = ResourceHelper.getDatabasePath()

private val GOLD_ZONE = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/GOLD_ZONE.zmap", PLUGIN_NAME)
private val GOLD_CONFIG = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/GOLD_CONFIG.xml", PLUGIN_NAME)
private val GOLEM_ZONE = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/GOLEM_ZONE.zmap", PLUGIN_NAME)
private val GOLEM_CONFIG = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/GOLEM_CONFIG.xml", PLUGIN_NAME)
private val E_TEMP_ZONE = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/E_RUINES_ZONE.zmap", PLUGIN_NAME)
private val E_TEMP_CONFIG = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/TEMP_CONFIG.xml", PLUGIN_NAME)
private val SPIDER_ZONE = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/SPIDER_ZONE.zmap", PLUGIN_NAME)
private val SPIDER_CONFIG = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/SPIDER_CONFIG.xml", PLUGIN_NAME)
private val WINDI_ZONE = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/WINDI_HIL.zmap", PLUGIN_NAME)
private val WINDI_CONFIG = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/WINDI_CONFIG.xml", PLUGIN_NAME)
private val ONE_STEP_ZONE = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/EO_STEP_ONE.zmap", PLUGIN_NAME)
private val ONE_TWO_ZONE = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/EO_STEP_TWO.zmap", PLUGIN_NAME)
private val ONE_STEP_CONFIG = ResourceHelper.extractPluginResource(PLUGIN_CL, "files/EO_STEP_ONE.xml", PLUGIN_NAME)

// ==================== Items ====================

private const val SOE_KM = 9647
private const val SOE = 10650
private const val SOE_DELAY = 35_000L
private const val BUFF_ID = 4328

// ==================== Quest IDs ====================

private const val QUEST_INITIAL = 174L
private const val QUEST_GOLD = 281L
private const val QUEST_TO15 = 10513L
private const val QUEST_SPIDER = 151L
private const val QUEST_PATH_ORC = 112L
private const val QUEST_PATH_SHUD = 122L
private const val QUEST_PROFF = 409L

// ==================== Item IDs ====================

private const val ITEM_QUEST_REWARD = 429
private const val ITEM_WEAPON_NG = 7816L
private const val ITEM_WEAPON_TOKEN = 7832
private const val ITEM_GOLD_QUEST = 9796
private const val ITEM_SPIDER_REWARD = 102
private const val ITEM_SPIDER_QUEST = 703
private const val ITEM_GOLEM_QUEST = 36037
private const val ITEM_RUINS_QUEST = 36038
private const val ITEM_PROFF_STEP1 = 1234
private const val ITEM_PROFF_STEP2 = 1236
private const val ITEM_PROFF_STEP3 = 1275
private const val ITEM_PROFF_STEP4 = 1232
private const val ITEM_PROFF_FINAL = 1233
private const val ITEM_CRYPTS_REWARD = 956

// ==================== Equipment sets ====================

private val STARTER_EQUIP_IDS =
    listOf(ITEM_WEAPON_NG.toInt(), ITEM_QUEST_REWARD.toInt(), 464, 43, 37, 49)
private val TOP_NG_EQUIP_IDS = listOf(35921, 35938, 35931, 35939, 35940)

// ==================== Mutable state ====================

private var levelSpider = 26
private var currentRelogIndex = -1

private fun log(tag: String, msg: String) {
    println("[GoldScript][$tag] $msg")
}

// ==================== Rebuff (generic) ====================

private suspend fun rebuff(bot: L2Bot, village: TownLocation, dialogIndex: Int) {
    val tag = "rebuff(${village::class.simpleName})"
    log(tag, "start, buffEnding=${bot.user().isBuffEnding(BUFF_ID)}")
    while (bot.user().isBuffEnding(BUFF_ID)) {
        bot.moveByKGpsToNpc(village.npcNewbie)
        bot.targetAndConfirm(village.npcNewbie.id)
        bot.openDialogAndConfirm()
        bot.selectedDialogByIndex(dialogIndex)
    }
    log(tag, "done")
}

private suspend fun rebuffKam(bot: L2Bot) = rebuff(bot, KamaelVillage, 5)
private suspend fun rebuffEV(bot: L2Bot) = rebuff(bot, ElvenVillage, 5)
private suspend fun rebuffTS(bot: L2Bot) = rebuff(bot, TalkingIsland, 5)
private suspend fun rebuffGludio(bot: L2Bot) = rebuff(bot, GludioVillage, 1)
private suspend fun rebuffGludin(bot: L2Bot) = rebuff(bot, GludinVillage, 1)

// ==================== Waypoints ====================

private suspend fun moveToRuins(bot: L2Bot) {
    bot.moveTo(28567, 74977, -3718)
    bot.moveTo(28160, 74980, -3809)
    bot.moveTo(27888, 74975, -3808)
    bot.moveTo(27548, 74960, -3808)
    bot.moveTo(27314, 74962, -3808)
    bot.moveTo(26981, 74963, -3897)
    bot.moveTo(26644, 74963, -4002)
    bot.moveTo(26323, 74961, -4098)
    bot.moveTo(26131, 74960, -4096)
    bot.moveTo(26009, 74959, -4096)
}

// ==================== Equipment ====================

private suspend fun equip(bot: L2Bot, ids: List<Int>, tag: String) {
    for (id in ids) {
        if (!bot.inventory().isEquipped(id)) {
            log(tag, "equipping item $id")
            while (!bot.inventory().isEquipped(id)) bot.useItemAndDelay(id, 1000)
        }
    }
}

private suspend fun equipment(bot: L2Bot) {
    log("equipment", "start, level=${bot.user().level}")

    while (bot.user().levelEq(6) && !bot.inventory().hasItemById(ITEM_WEAPON_NG.toInt())) {
        log("equipment", "level=6, getting items $ITEM_WEAPON_TOKEN -> $ITEM_WEAPON_NG")
        bot.moveByKGpsToNpc(KamaelVillage.npcNewbie)
        while (!bot.inventory().hasItemById(ITEM_WEAPON_TOKEN)) {
            bot.targetAndConfirm(KamaelVillage.npcNewbie.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(7)
            bot.selectedDialogByIndex(1)
        }
        log("equipment", "got $ITEM_WEAPON_TOKEN, exchanging for $ITEM_WEAPON_NG")
        while (!bot.inventory().hasItemById(ITEM_WEAPON_NG.toInt())) {
            bot.targetAndConfirm(KamaelVillage.npcNewbie.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(7)
            bot.selectedDialogByIndex(2)
            bot.npcExchange(ITEM_WEAPON_NG, 1)
        }
    }

    equip(bot, STARTER_EQUIP_IDS, "equipment")
    log("equipment", "done")
}

private suspend fun equipmentTopNg(bot: L2Bot) {
    log("equipmentTopNg", "start")

    if (!bot.inventory().hasItemById(35921)) {
        log("equipmentTopNg", "exchanging 35916 -> 35921")
        while (!bot.inventory().hasItemById(35921)) {
            bot.useItemAndDelay(35916, 100)
            bot.npcExchange(35921, 1)
            delay(1000)
        }
    }
    if (!bot.inventory().hasItemById(35938)) {
        log("equipmentTopNg", "exchanging 35917 -> 35938")
        while (!bot.inventory().hasItemById(35938)) {
            bot.useItemAndDelay(35917, 100)
            bot.npcExchange(35938, 1)
            delay(1000)
        }
    }

    equip(bot, TOP_NG_EQUIP_IDS, "equipmentTopNg")
    delay(1000)
    log("equipmentTopNg", "done")
}

// ==================== StartQuest (до 6 уровня) ====================

private suspend fun startQuest(bot: L2Bot) {
    log("startQuest", "start, level=${bot.user().level}")
    while (!bot.user().levelGte(6)) {
        log(
            "startQuest", "loop, level=${bot.user().level}, " +
                    "q$QUEST_INITIAL: s1=${bot.questStatus(QUEST_INITIAL, 1)}, " +
                    "s2=${bot.questStatus(QUEST_INITIAL, 2)}, " +
                    "s3=${bot.questStatus(QUEST_INITIAL, 3)}"
        )

        val hasEndItem = bot.inventory().hasItemById(ITEM_QUEST_REWARD)
        if (!hasEndItem) {
            while (!bot.questStatus(QUEST_INITIAL, 1) && !bot.questStatus(QUEST_INITIAL, 3)) {
                log("startQuest", "taking quest from NPC_Markela")
                bot.moveByKGpsToNpc(KamaelVillage.npcMarkela)
                bot.targetAndConfirm(KamaelVillage.npcMarkela.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(3)
                bot.selectedDialogByText("Проверка")
                bot.selectedDialogByIndex(1)
            }
            while (bot.questStatus(QUEST_INITIAL, 1) && !bot.questStatus(QUEST_INITIAL, 2)) {
                log("startQuest", "stage1 -> going to NPC_Benis")
                bot.moveByKGpsToNpc(KamaelVillage.npcBenis)
                bot.targetAndConfirm(KamaelVillage.npcBenis.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(5)
            }
            while (bot.questStatus(QUEST_INITIAL, 2)) {
                log("startQuest", "stage2 -> Markela + Grocer")
                bot.moveByKGpsToNpc(KamaelVillage.npcMarkela)
                bot.targetAndConfirm(KamaelVillage.npcMarkela.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(3)
                bot.selectedDialogByIndex(1)
                bot.moveByKGpsToNpc(KamaelVillage.npcGrocer)
                bot.targetAndConfirm(KamaelVillage.npcGrocer.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(3)
            }
            while (bot.questStatus(QUEST_INITIAL, 3)) {
                log("startQuest", "stage3 -> turn in at Markela")
                bot.moveByKGpsToNpc(KamaelVillage.npcMarkela)
                bot.targetAndConfirm(KamaelVillage.npcMarkela.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(3)
                bot.selectedDialogByIndex(1)
            }
        } else {
            log("startQuest", "up level to 6")
            bot.moveByKGpsToNpc(KamaelVillage.npcNewbie)
            bot.targetAndConfirm(KamaelVillage.npcNewbie.id)
            bot.openDialogAndConfirm()
        }
    }
    log("startQuest", "done, level=${bot.user().level}")
}

// ==================== GoldQuest (до 9 уровня) ====================

private suspend fun goldQuest(bot: L2Bot) {
    log("goldQuest", "start, level=${bot.user().level}")
    while (!bot.user().levelGte(9)) {
        if (bot.user().isBuffEnding(BUFF_ID)) {
            log("goldQuest", "buff ending, breaking out")
            break
        }
        while (!bot.questStatus(QUEST_GOLD, 1)) {
            log("goldQuest", "taking quest $QUEST_GOLD from Markela")
            bot.moveByKGpsToNpc(KamaelVillage.npcMarkela)
            bot.targetAndConfirm(KamaelVillage.npcMarkela.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByText("Квест")
            bot.selectedDialogByText("Золотые")
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
        }

        log(
            "goldQuest", "farming, q${QUEST_GOLD}_stage1=${bot.questStatus(QUEST_GOLD, 1)}, " +
                    "hasItem$ITEM_GOLD_QUEST=${bot.hasQuestItem(ITEM_GOLD_QUEST)}"
        )

        while (bot.questStatus(QUEST_GOLD, 1)
            && !bot.user().isBuffEnding(BUFF_ID)
            && !bot.hasQuestItem(ITEM_GOLD_QUEST)
        ) {
            if (bot.isInLocation(KamaelVillage)) {
                log("goldQuest", "in Kamael village, teleporting to gold farm")
                bot.moveByKGpsToNpc(KamaelVillage.npcGatekeeper)
                bot.targetAndConfirm(KamaelVillage.npcGatekeeper.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(1)
                bot.selectedDialogByIndex(8)
                delay(15000)
                bot.moveTo(-122292, 73801, -2872)
                bot.moveTo(-122493, 74582, -3041)
                bot.moveTo(-123152, 76061, -3248)
                bot.loadZone(GOLD_ZONE)
                bot.loadConfig(GOLD_CONFIG)
                bot.setFaceControl(0, true)
                log("goldQuest", "faceControl ON, farming started")
            }
            if (bot.hasQuestItem(ITEM_GOLD_QUEST)) {
                log("goldQuest", "got quest item $ITEM_GOLD_QUEST, using SoE")
                bot.setFaceControl(0, false)
                bot.useItemAndDelay(SOE, SOE_DELAY)
                break
            }
            delay(100)
        }

        if (bot.user().inZone && bot.hasQuestItem(ITEM_GOLD_QUEST)) {
            log("goldQuest", "in zone with item $ITEM_GOLD_QUEST, using SoE")
            bot.setFaceControl(0, false)
            bot.useItemAndDelay(SOE, SOE_DELAY)
        }

        while (bot.questStatus(QUEST_GOLD, 1)
            && bot.hasQuestItem(ITEM_GOLD_QUEST)
            && bot.isInLocation(KamaelVillage)
        ) {
            log("goldQuest", "turning in quest $QUEST_GOLD")
            bot.moveByKGpsToNpc(KamaelVillage.npcMarkela)
            bot.targetAndConfirm(KamaelVillage.npcMarkela.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(3)
            bot.selectedDialogByText("Золотые")
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            bot.moveByKGpsToNpc(KamaelVillage.npcNewbie)
            bot.targetAndConfirm(KamaelVillage.npcNewbie.id)
            bot.openDialogAndConfirm()
        }
        delay(100)
    }
    log("goldQuest", "done, level=${bot.user().level}")
}

// ==================== To15Stage (до 15 уровня) ====================

private suspend fun to15Stage(bot: L2Bot) {
    log("to15Stage", "start, level=${bot.user().level}")
    while (!bot.user().levelGte(15)) {
        log(
            "to15Stage", "loop, level=${bot.user().level}, " +
                    "q$QUEST_TO15: s1=${bot.questStatus(QUEST_TO15, 1)}, " +
                    "s2=${bot.questStatus(QUEST_TO15, 2)}, s3=${bot.questStatus(QUEST_TO15, 3)}"
        )

        // Берём квест
        while (!bot.questStatus(QUEST_TO15, 1)
            && !bot.questStatus(QUEST_TO15, 2)
            && !bot.questStatus(QUEST_TO15, 3)
        ) {
            log("to15Stage", "taking quest $QUEST_TO15")
            if (bot.user().isBuffEnding(BUFF_ID) && !bot.user().isDead) rebuffKam(bot)
            bot.moveByKGpsToNpc(KamaelVillage.npcServer)
            bot.targetAndConfirm(KamaelVillage.npcServer.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(3)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
        }

        // Стадия 2 — в локации големов, возврат
        if (bot.isInLocation(-94407, 50082, -2480, 5000) && bot.questStatus(QUEST_TO15, 2)) {
            log("to15Stage", "golem loc + stage2 -> SoE back")
            bot.setFaceControl(0, false)
            delay(100)
            bot.useItemAndDelay(SOE_KM, SOE_DELAY)
            bot.leaveParty()
        }

        // В локации големов, есть бафф, мало квест-итемов
        if (bot.isInLocation(-94407, 50082, -2480, 5000)
            && bot.user().hasBuff(BUFF_ID)
            && bot.getQuestItemCount(ITEM_GOLEM_QUEST) < 50
        ) {
            log(
                "to15Stage", "golem loc + buff + items($ITEM_GOLEM_QUEST)=" +
                        "${bot.getQuestItemCount(ITEM_GOLEM_QUEST)} < 50 -> loading golem zone"
            )
            bot.loadZone(GOLEM_ZONE)
            bot.loadConfig(GOLEM_CONFIG)
            bot.setFaceControl(0, true)
            delay(1000)
        }

        // В Камалоке, мало квест-итемов, не стадия 2
        if (bot.isInLocation(KamaelVillage)
            && bot.getQuestItemCount(ITEM_GOLEM_QUEST) < 50
            && !bot.questStatus(QUEST_TO15, 2)
        ) {
            log("to15Stage", "kamael loc + items<50 + not stage2 -> teleport to quest")
            rebuffKam(bot)
            bot.moveByKGpsToNpc(KamaelVillage.npcGatekeeper)
            bot.targetAndConfirm(KamaelVillage.npcGatekeeper.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(3)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
        }

        // В Камалоке, стадия 2, не стадия 3 — путь к руинам
        if (bot.isInLocation(KamaelVillage)
            && bot.questStatus(QUEST_TO15, 2)
            && !bot.questStatus(QUEST_TO15, 3)
        ) {
            log("to15Stage", "kamael loc + stage2 + not stage3 -> moving to ruins")
            rebuffKam(bot)
            bot.setFaceControl(0, false)
            bot.moveByKGpsToNpc(KamaelVillage.npcServer)
            bot.targetAndConfirm(KamaelVillage.npcServer.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(3)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            delay(25000)
            moveToRuins(bot)
            log("to15Stage", "arrived at ruins")
        }

        // В руинах, мало квест-итемов, есть бафф
        if (bot.isInLocation(26411, 74907, -4072, 5000)
            && bot.getQuestItemCount(ITEM_RUINS_QUEST) < 50
            && bot.user().hasBuff(BUFF_ID)
        ) {
            log(
                "to15Stage", "ruins + items($ITEM_RUINS_QUEST)=" +
                        "${bot.getQuestItemCount(ITEM_RUINS_QUEST)} < 50 + buff -> loading ruins zone"
            )
            bot.loadZone(E_TEMP_ZONE)
            bot.loadConfig(E_TEMP_CONFIG)
            bot.setFaceControl(0, true)
            delay(1000)
        }

        // В руинах, стадия 3
        if (bot.isInLocation(26411, 74907, -4072, 5000)
            && bot.questStatus(QUEST_TO15, 3)
            && !bot.user().isDead
        ) {
            log("to15Stage", "ruins + stage3 -> SoE + leave party")
            bot.setFaceControl(0, false)
            bot.useItemAndDelay(SOE, SOE_DELAY)
            bot.leaveParty()
        }

        if (bot.user().isDead) {
            log("to15Stage", "dead -> goHome")
            bot.goHome()
        }

        // В Elven Village, стадия 3
        if (bot.isInLocation(ElvenVillage) && bot.questStatus(QUEST_TO15, 3)) {
            log("to15Stage", "elven village + stage3 -> turn in")
            rebuffEV(bot)
            bot.moveByKGpsToNpc(ElvenVillage.npcServer)
            bot.targetAndConfirm(ElvenVillage.npcServer.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(3)
            bot.selectedDialogByIndex(1)
        }

        // В Elven Village, стадия 2
        if (bot.isInLocation(ElvenVillage) && bot.questStatus(QUEST_TO15, 2)) {
            log("to15Stage", "elven village + stage2 -> moving to ruins")
            rebuffEV(bot)
            bot.moveByKGpsToNpc(ElvenVillage.npcServer)
            bot.targetAndConfirm(ElvenVillage.npcServer.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(3)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            delay(25000)
            moveToRuins(bot)
            log("to15Stage", "arrived at ruins from EV")
        }

        delay(1000)
    }
    log("to15Stage", "done, level=${bot.user().level}")
}

// ==================== PathSpider (до levelSpider) ====================

private suspend fun pathSpider(bot: L2Bot) {
    log("pathSpider", "start, level=${bot.user().level}, levelSpider=$levelSpider")
    bot.useItemAndDelay(SOE_KM, SOE_DELAY)

    while (!bot.user().levelGte(levelSpider)) {
        log(
            "pathSpider",
            "loop, level=${bot.user().level}, q$QUEST_SPIDER=${bot.questStatus(QUEST_SPIDER)}"
        )

        // Телепорт на Talking Island
        while (!bot.isInLocation(TalkingIsland) && !bot.questStatus(QUEST_SPIDER, 1)) {
            log("pathSpider", "teleporting to Talking Island")
            bot.moveByKGpsToNpc(KamaelVillage.npcGatekeeper)
            bot.targetAndConfirm(KamaelVillage.npcGatekeeper.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByText("Talking Island")
            delay(15000)
        }

        // Берём квест у Elias
        while (bot.isInLocation(TalkingIsland)
            && !bot.questStatus(QUEST_SPIDER, 1)
            && !bot.inventory().hasItemById(ITEM_SPIDER_REWARD)
        ) {
            log("pathSpider", "taking quest $QUEST_SPIDER from NPC_Elias")
            bot.moveByKGpsToNpc(TalkingIsland.npcElias)
            bot.targetAndConfirm(TalkingIsland.npcElias.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
        }

        // Квест стадия 1
        while (bot.questStatus(QUEST_SPIDER, 1)) {
            log(
                "pathSpider",
                "stage1, items($ITEM_SPIDER_QUEST)=${bot.getQuestItemCount(ITEM_SPIDER_QUEST)}, " +
                        "hasItem$ITEM_SPIDER_REWARD=${
                            bot.inventory().hasItemById(ITEM_SPIDER_REWARD)
                        }"
            )

            if (bot.isInLocation(TalkingIsland)
                && bot.getQuestItemCount(ITEM_SPIDER_QUEST) < 1
                && !bot.questStatus(QUEST_SPIDER, 2)
                && !bot.inventory().hasItemById(ITEM_SPIDER_REWARD)
            ) {
                log("pathSpider", "TI + no items -> teleporting to spider spot")
                rebuffTS(bot)
                bot.moveByKGpsToNpc(TalkingIsland.npcGatekeeper)
                bot.targetAndConfirm(TalkingIsland.npcGatekeeper.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(3)
                bot.selectedDialogByIndex(8)
                delay(15000)
            }

            if (bot.isInLocation(-111644, 244338, -3448, 200)) {
                log("pathSpider", "at spider path start, moving to spot")
                bot.moveTo(-111975, 244367, -3448)
                bot.moveTo(-112358, 244415, -3448)
                bot.moveTo(-112820, 244461, -3448)
                bot.moveTo(-113156, 244516, -3431)
                bot.moveTo(-113462, 244580, -3415)
                bot.moveTo(-113678, 244635, -3391)
                bot.moveTo(-113904, 244761, -3350)
                bot.moveTo(-114156, 244933, -3311)
                bot.moveTo(-114369, 245083, -3287)
                bot.moveTo(-114410, 245106, -3288)
                bot.moveTo(-114575, 245096, -3262)
                bot.loadZone(SPIDER_ZONE)
                bot.loadConfig(SPIDER_CONFIG)
                bot.setFaceControl(0, true)
                log("pathSpider", "spider farm started")
            }

            if (bot.user().isDead) {
                log("pathSpider", "dead in stage1 -> goHome")
                bot.setFaceControl(0, false)
                bot.goHome()
            }
            delay(100)
        }

        // Квест стадия 2
        while (bot.questStatus(QUEST_SPIDER, 2)) {
            log(
                "pathSpider",
                "stage2, items($ITEM_SPIDER_QUEST)=${bot.getQuestItemCount(ITEM_SPIDER_QUEST)}, " +
                        "hasItem$ITEM_SPIDER_REWARD=${
                            bot.inventory().hasItemById(ITEM_SPIDER_REWARD)
                        }"
            )

            if (bot.user().isDead) {
                log("pathSpider", "dead in stage2 -> goHome")
                bot.setFaceControl(0, false)
                bot.goHome()
            }

            if (bot.isInLocation(-114461, 245289, -3272, 5000) && bot.questStatus(
                    QUEST_SPIDER,
                    2
                )
            ) {
                log("pathSpider", "at spider spot + stage2 complete -> SoE")
                bot.setFaceControl(0, false)
                bot.useItemAndDelay(SOE, SOE_DELAY)
            }

            if (bot.isInLocation(TalkingIsland)
                && bot.questStatus(QUEST_SPIDER, 2)
                && bot.getQuestItemCount(ITEM_SPIDER_QUEST) > 0
            ) {
                log("pathSpider", "TI + items>0 -> turning in at NPC_Yohanes")
                rebuffTS(bot)
                while (bot.getQuestItemCount(ITEM_SPIDER_QUEST) > 0) {
                    bot.moveByKGpsToNpc(TalkingIsland.npcYohanes)
                    bot.targetAndConfirm(TalkingIsland.npcYohanes.id)
                    bot.openDialogAndConfirm()
                    bot.selectedDialogByText("Квест")
                }
            }

            if (bot.isInLocation(TalkingIsland)
                && bot.questStatus(QUEST_SPIDER, 2)
                && !bot.inventory().hasItemById(ITEM_SPIDER_REWARD)
            ) {
                log("pathSpider", "TI + no item $ITEM_SPIDER_REWARD -> talking to Elias")
                bot.moveByKGpsToNpc(TalkingIsland.npcElias)
                bot.targetAndConfirm(TalkingIsland.npcElias.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(1)
            }
        }

        // Сдаём reward и телепортируемся
        if (bot.isInLocation(TalkingIsland) && bot.inventory().hasItemById(ITEM_SPIDER_REWARD)) {
            log("pathSpider", "has item $ITEM_SPIDER_REWARD -> NPC_Newbie + teleport")
            bot.moveByKGpsToNpc(TalkingIsland.npcNewbie)
            bot.targetAndConfirm(TalkingIsland.npcNewbie.id)
            bot.openDialogAndConfirm()
            levelSpider = 24 // TODO()
            log("pathSpider", "levelSpider set to $levelSpider")
            bot.moveByKGpsToNpc(TalkingIsland.npcGatekeeper)
            bot.targetAndConfirm(TalkingIsland.npcGatekeeper.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(3)
            bot.selectedDialogByIndex(1)
            delay(15000)
        }

        delay(100)
    }
    log("pathSpider", "done, level=${bot.user().level}")
}

// ==================== RunExp ====================

private suspend fun checkLocationExp(bot: L2Bot) {
    if (!bot.isInLocation(GludinVillage)) {
        log("checkLocationExp", "not in Gludin area, teleporting from Kamael")
        bot.useItemAndDelay(SOE_KM, SOE_DELAY)
        bot.moveByKGpsToNpc(KamaelVillage.npcGatekeeper)
        bot.targetAndConfirm(KamaelVillage.npcGatekeeper.id)
        bot.openDialogAndConfirm()
        bot.selectedDialogByIndex(1)
        bot.selectedDialogByIndex(1)
        delay(15000)
        log("checkLocationExp", "arrived at Gludio, teleporting to Gludin")
        bot.targetAndConfirm(GludioVillage.npcGatekeeper.id)
        bot.openDialogAndConfirm()
        bot.selectedDialogByIndex(2)
        bot.selectedDialogByIndex(9)
        delay(15000)
        log("checkLocationExp", "arrived at Gludin")
    } else {
        log("checkLocationExp", "already in Gludin area")
    }
}

private suspend fun moveToSpot(bot: L2Bot) {
    if (bot.isInLocation(GludinVillage)) {
        log("moveToSpot", "in Gludin, rebuffing and teleporting to exp spot")
        rebuffGludin(bot)
        bot.moveByKGpsToNpc(GludinVillage.npcGatekeeper)
        bot.targetAndConfirm(GludinVillage.npcGatekeeper.id)
        bot.openDialogAndConfirm()
        bot.selectedDialogByIndex(1)
        bot.selectedDialogByIndex(9)
        delay(15000)
        log("moveToSpot", "arrived at exp spot")
    }
}

private suspend fun exping(bot: L2Bot) {
    log("exping", "start, loading zone and config")
    bot.loadZone(WINDI_ZONE)
    bot.loadConfig(WINDI_CONFIG)
    bot.setFaceControl(0, true)
    delay(15000)

    while (bot.user().inZone) {
        bot.loadZone(WINDI_ZONE)
        bot.loadConfig(WINDI_CONFIG)
        bot.setFaceControl(0, true)

        if (bot.user().isDead) {
            log("exping", "dead -> goHome")
            bot.goHome()
            bot.setFaceControl(0, false)
        }
        if (bot.user().level > 19) {
            log("exping", "level=${bot.user().level} > 19 -> SoE, stopping farm")
            bot.useItemAndDelay(SOE, SOE_DELAY)
            bot.setFaceControl(0, false)
        }
        delay(1000)
    }
    log("exping", "left zone")
}

private suspend fun runExp(bot: L2Bot) {
    log("runExp", "start, level=${bot.user().level}")
    while (bot.user().level < 20) {
        log("runExp", "loop, level=${bot.user().level}")
        checkLocationExp(bot)
        moveToSpot(bot)
        exping(bot)
        delay(1000)
    }
    log("runExp", "level >= 20, returning to town")
    while (!bot.isInLocation(GludinVillage)) {
        if (bot.user().isDead) {
            log("runExp", "dead while returning -> goHome")
            bot.goHome()
        }
        if (!bot.user().isCast()) {
            log("runExp", "not casting -> using SoE")
            bot.useItemAndDelay(SOE, SOE_DELAY)
        }
        delay(1000)
    }
    log("runExp", "done, back in town")
}

// ==================== RunCrypts ====================

private suspend fun pathOrcs(bot: L2Bot) {
    log("pathOrcs", "start, q$QUEST_PATH_ORC=${bot.questStatus(QUEST_PATH_ORC)}")
    if (bot.questStatus(QUEST_PATH_ORC) == 0L) {
        bot.useItemAndDelay(SOE_KM, SOE_DELAY)
        while (bot.questStatus(QUEST_PATH_ORC) == 0L) {
            if (bot.isInLocation(KamaelVillage)) {
                log("pathOrcs", "kamael loc -> teleporting to orcs")
                rebuffKam(bot)
                bot.moveByKGpsToNpc(KamaelVillage.npcGatekeeper)
                bot.targetAndConfirm(KamaelVillage.npcGatekeeper.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(1)
                bot.selectedDialogByIndex(7)
                delay(15000)
            }
            if (bot.isInLocation(OrcVillage)) {
                log("pathOrcs", "at orc village -> talking to Livina")
                bot.moveByKGpsToNpc(OrcVillage.npcLivina)
                bot.targetAndConfirm(OrcVillage.npcLivina.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(3)
                bot.selectedDialogByText("Путь судьбы")
                bot.selectedDialogByIndex(1)
                bot.selectedDialogByIndex(1)
            }
            delay(1000)
        }
    }
    log("pathOrcs", "done, q$QUEST_PATH_ORC=${bot.questStatus(QUEST_PATH_ORC)}")
}

private suspend fun pathShud(bot: L2Bot) {
    log("pathShud", "start, q$QUEST_PATH_SHUD=${bot.questStatus(QUEST_PATH_SHUD)}")
    if (bot.questStatus(QUEST_PATH_SHUD) == 0L) {
        while (bot.questStatus(QUEST_PATH_SHUD) == 0L) {
            if (bot.isInLocation(OrcVillage)) {
                log("pathShud", "at orc village -> teleporting to Schuttgart")
                bot.moveByKGpsToNpc(OrcVillage.npcGatekeeper)
                bot.targetAndConfirm(OrcVillage.npcGatekeeper.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(1)
                bot.selectedDialogByIndex(2)
                delay(15000)
                log("pathShud", "talking to Moira")
                bot.moveByKGpsToNpc(SchuttgartTown.npcMoira)
                bot.targetAndConfirm(SchuttgartTown.npcMoira.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(2)
                bot.selectedDialogByText("Тревожные")
                bot.selectedDialogByIndex(1)
                bot.selectedDialogByIndex(1)
            } else {
                log("pathShud", "not at orcs -> SoE + rebuff + teleport to orcs")
                bot.useItemAndDelay(SOE_KM, SOE_DELAY)
                rebuffKam(bot)
                bot.moveByKGpsToNpc(KamaelVillage.npcGatekeeper)
                bot.targetAndConfirm(KamaelVillage.npcGatekeeper.id)
                bot.openDialogAndConfirm()
                bot.selectedDialogByIndex(7)
                delay(15000)
            }
        }
    }
    log("pathShud", "done, q$QUEST_PATH_SHUD=${bot.questStatus(QUEST_PATH_SHUD)}")
}

private suspend fun doneQuestStage(bot: L2Bot) {
    log(
        "doneQuestStage", "start, q$QUEST_PATH_ORC=${bot.questStatus(QUEST_PATH_ORC)}, " +
                "q$QUEST_PATH_SHUD=${bot.questStatus(QUEST_PATH_SHUD)}"
    )
    if (bot.questStatus(QUEST_PATH_ORC) == 0L || bot.questStatus(QUEST_PATH_SHUD) == 0L) {
        log("doneQuestStage", "quests not done yet, running pathOrcs + pathShud")
        pathOrcs(bot)
        pathShud(bot)
    } else {
        log("doneQuestStage", "both quests active, teleporting to turn-in NPC")
        bot.moveByKGpsToNpc(SchuttgartTown.npcGatekeeper)
        bot.targetAndConfirm(SchuttgartTown.npcGatekeeper.id)
        bot.openDialogAndConfirm()
        bot.selectedDialogByIndex(1)
        bot.selectedDialogByText("17. Crypts of Disgrace (81-82)")
        bot.moveTo(47666, -115910, -3744)
        while (bot.questStatus(QUEST_PATH_ORC) != 0L || bot.questStatus(QUEST_PATH_SHUD) != 0L) {
            log(
                "doneQuestStage", "turning in quests, " +
                        "q$QUEST_PATH_ORC=${bot.questStatus(QUEST_PATH_ORC)}, " +
                        "q$QUEST_PATH_SHUD=${bot.questStatus(QUEST_PATH_SHUD)}"
            )
            bot.targetAndConfirm(32017)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByText("Путь")
            bot.selectedDialogByIndex(1)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByText("Тревожные")
            bot.selectedDialogByIndex(1)
        }
    }
    log("doneQuestStage", "done")
}

private suspend fun runCrypts(bot: L2Bot) {
    log("runCrypts", "start")
    pathOrcs(bot)
    pathShud(bot)
    doneQuestStage(bot)
    log("runCrypts", "done")
}

// ==================== RunQuest / ProfEo ====================

private suspend fun gludinVillageToGludio(bot: L2Bot) {
    log("gludinToGludio", "teleporting Gludin -> Gludio")
    bot.moveByKGpsToNpc(GludinVillage.npcGatekeeper)
    bot.targetAndConfirm(GludinVillage.npcGatekeeper.id)
    bot.openDialogAndConfirm()
    bot.selectedDialogByIndex(1)
    bot.selectedDialogByIndex(1)
    delay(15000)
    log("gludinToGludio", "arrived")
}

private suspend fun kamToGludio(bot: L2Bot) {
    log("kamToGludio", "SoE + teleporting Kamael -> Gludio")
    bot.useItemAndDelay(SOE_KM, SOE_DELAY)
    bot.moveByKGpsToNpc(KamaelVillage.npcGatekeeper)
    bot.targetAndConfirm(KamaelVillage.npcGatekeeper.id)
    bot.openDialogAndConfirm()
    bot.selectedDialogByIndex(1)
    bot.selectedDialogByIndex(1)
    delay(15000)
    log("kamToGludio", "arrived")
}

private suspend fun firstStage(bot: L2Bot) {
    log("firstStage", "start, q$QUEST_PROFF=${bot.questStatus(QUEST_PROFF)}")

    // Стадия 1
    while (bot.questStatus(QUEST_PROFF) == 1L) {
        log("firstStage", "stage1 loop")
        if (bot.isInLocation(GludioVillage)) {
            log("firstStage", "at Gludio -> teleporting to Gludin")
            bot.moveByKGpsToNpc(GludioVillage.npcGatekeeper)
            bot.targetAndConfirm(GludioVillage.npcGatekeeper.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(2)
            bot.selectedDialogByIndex(9)
            delay(15000)
        }
        if (bot.isInLocation(GludinVillage)) {
            log("firstStage", "near Gludin -> moving to NPC_Allana")
            bot.moveByKGpsToNpc(GludinVillage.npcAllana)
        }
        if (bot.isRangeNpc(GludinVillage.npcAllana, 500)) {
            log("firstStage", "at Allana -> talking")
            bot.targetAndConfirm(GludinVillage.npcAllana.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(2)
        }
        delay(1000)
    }

    // Стадия 2
    while (bot.questStatus(QUEST_PROFF) == 2L) {
        log(
            "firstStage",
            "stage2 loop, items($ITEM_PROFF_STEP1)=${bot.getQuestItemCount(ITEM_PROFF_STEP1)}"
        )
        if (bot.isRangeNpc(
                GludinVillage.npcAllana,
                500
            ) && bot.getQuestItemCount(ITEM_PROFF_STEP1) < 1
        ) {
            log("firstStage", "at Allana + no items -> loading farm zone")
            bot.loadZone(ONE_STEP_ZONE)
            bot.loadConfig(ONE_STEP_CONFIG)
            bot.setFaceControl(0, true)
            delay(25000)
        }
        if (bot.isRangeNpc(
                GludinVillage.npcAllana,
                500
            ) && bot.getQuestItemCount(ITEM_PROFF_STEP1) > 0
        ) {
            log("firstStage", "at Allana + has items -> turning in")
            bot.setFaceControl(0, false)
            bot.targetAndConfirm(GludinVillage.npcAllana.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
        }
        if (bot.user().isDead) {
            log("firstStage", "dead in stage2 -> goHome")
            bot.goHome()
        }
        if (bot.isInLocation(GludinVillage)) {
            log("firstStage", "respawned near Gludin -> rebuff + go to Allana")
            rebuffGludin(bot)
            bot.moveGpsPoint("NPC_Allana")
        }
        if (bot.isRangeNpc(
                GludinVillage.npcAllana,
                500
            ) && bot.getQuestItemCount(ITEM_PROFF_STEP1) < 1
        ) {
            log("firstStage", "at Allana + still no items -> re-talk")
            bot.targetAndConfirm(GludinVillage.npcAllana.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(2)
        }
        delay(1000)
    }

    // Стадия 3
    while (bot.questStatus(QUEST_PROFF) == 3L) {
        log(
            "firstStage", "stage3 loop, items: " +
                    "$ITEM_PROFF_STEP2=${bot.getQuestItemCount(ITEM_PROFF_STEP2)}, " +
                    "$ITEM_PROFF_STEP3=${bot.getQuestItemCount(ITEM_PROFF_STEP3)}, " +
                    "$ITEM_PROFF_STEP4=${bot.getQuestItemCount(ITEM_PROFF_STEP4)}, " +
                    "$ITEM_PROFF_FINAL=${bot.getQuestItemCount(ITEM_PROFF_FINAL)}"
        )

        if (bot.isRangeNpc(
                GludinVillage.npcAllana,
                500
            ) && bot.getQuestItemCount(ITEM_PROFF_STEP2) > 0
        ) {
            log("firstStage", "at Allana + has $ITEM_PROFF_STEP2 -> going to NPC_Perrin")
            bot.moveByKGpsToNpc(GludinVillage.npcPerrin)
        }
        if (bot.isRangeNpc(
                GludinVillage.npcPerrin,
                500
            ) && bot.getQuestItemCount(ITEM_PROFF_STEP3) < 1
        ) {
            log("firstStage", "at Perrin + no $ITEM_PROFF_STEP3 -> starting farm")
            bot.targetAndConfirm(GludinVillage.npcPerrin.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            bot.loadZone(ONE_TWO_ZONE)
            bot.setFaceControl(0, true)
            delay(20000)
        }
        if (bot.isRangeNpc(GludinVillage.npcPerrin, 500)
            && bot.getQuestItemCount(ITEM_PROFF_STEP3) > 0
            && bot.getQuestItemCount(ITEM_PROFF_STEP4) < 1
        ) {
            log(
                "firstStage",
                "at Perrin + has $ITEM_PROFF_STEP3, no $ITEM_PROFF_STEP4 -> turning in"
            )
            bot.targetAndConfirm(GludinVillage.npcPerrin.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
        }
        if (bot.isRangeNpc(
                GludinVillage.npcPerrin,
                500
            ) && bot.getQuestItemCount(ITEM_PROFF_STEP4) > 0
        ) {
            log("firstStage", "at Perrin + has $ITEM_PROFF_STEP4 -> going to Allana")
            bot.moveByKGpsToNpc(GludinVillage.npcAllana)
        }
        if (bot.isRangeNpc(
                GludinVillage.npcAllana,
                500
            ) && bot.getQuestItemCount(ITEM_PROFF_STEP4) > 0
        ) {
            log("firstStage", "at Allana + has $ITEM_PROFF_STEP4 -> turning in")
            bot.targetAndConfirm(GludinVillage.npcAllana.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
        }
        if (bot.isRangeNpc(
                GludinVillage.npcAllana,
                500
            ) && bot.getQuestItemCount(ITEM_PROFF_FINAL) > 0
        ) {
            log("firstStage", "at Allana + has $ITEM_PROFF_FINAL -> FINAL, going to NPC_Manuel")
            bot.moveByKGpsToNpc(GludinVillage.npcGatekeeper)
            bot.targetAndConfirm(GludinVillage.npcGatekeeper.id)
            bot.openDialogAndConfirm()
            bot.selectedDialogByIndex(1)
            bot.selectedDialogByIndex(1)
            bot.moveByKGpsToNpc(GludioVillage.npcManuel)
            break
        }
        if (bot.user().isDead) {
            log("firstStage", "dead in stage3 -> goHome")
            bot.goHome()
        }
        if (bot.isInLocation(GludinVillage)) {
            log("firstStage", "respawned -> rebuff + go to Perrin")
            rebuffGludin(bot)
            bot.moveByKGpsToNpc(GludinVillage.npcPerrin)
        }
        delay(1000)
    }
    log("firstStage", "done, q$QUEST_PROFF=${bot.questStatus(QUEST_PROFF)}")
}

private suspend fun pathToStartNpc(bot: L2Bot) {
    log("pathToStartNpc", "start, q$QUEST_PROFF=${bot.questStatus(QUEST_PROFF)}")
    while (bot.questStatus(QUEST_PROFF) == 0L) {
        if (bot.isInLocation(GludinVillage)) {
            log("pathToStartNpc", "at Gludin -> gludinToGludio")
            gludinVillageToGludio(bot)
        } else {
            log("pathToStartNpc", "not at Gludin -> kamToGludio")
            kamToGludio(bot)
        }
        rebuffGludio(bot)
        log("pathToStartNpc", "talking to NPC_Manuel")
        bot.moveByKGpsToNpc(GludioVillage.npcManuel)
        bot.targetAndConfirm(GludioVillage.npcManuel.id)
        bot.openDialogAndConfirm()
        bot.selectedDialogByText("Квест")
        bot.selectedDialogByIndex(1)
        bot.selectedDialogByIndex(1)
    }
    firstStage(bot)
    log("pathToStartNpc", "done")
}

private suspend fun runQuest(bot: L2Bot) {
    log("runQuest", "start")
    pathToStartNpc(bot)
    log("runQuest", "done")
}

// ==================== RelogControl ====================

private suspend fun relogControl(bot: L2Bot) {
    currentRelogIndex++
    log("relogControl", "relog to charIndex=$currentRelogIndex")
    bot.relog(currentRelogIndex)
    if (currentRelogIndex == 6) {
        currentRelogIndex = -1
        log("relogControl", "reset index to -1")
    }
}

// ==================== Controller ====================

private suspend fun controller(bot: L2Bot) {
    while (bot.getGameWindowHandle() != 0L) {
        log("controller", "=== SCRIPT STARTED ===")

        if (bot.user().levelLte(6)) {
            log("controller", "-> startQuest (level <= 6)")
            if (!bot.isInLocation(KamaelVillage)) {
                log("controller", "to kam")
                bot.useItemAndDelay(SOE_KM, SOE_DELAY)
            }
            startQuest(bot)
        }

        bot.user().let { user ->
            if (user.levelGte(6) && user.levelLte(10) && !bot.inventory()
                    .isEquipped(ITEM_WEAPON_NG.toInt())
            ) {
                log("controller", "-> equipment (6-10, no $ITEM_WEAPON_NG)")
                equipment(bot)
            }
        }
        bot.user().let { user ->
            if (user.levelGte(6) && user.levelLte(10)) {
                log("controller", "-> goldQuest (6-10)")
                if (user.isBuffEnding(BUFF_ID)) rebuffKam(bot)
                goldQuest(bot)
            }
        }
        bot.user().let { user ->
            if (user.levelGte(9) && user.levelLte(15)) {
                log("controller", "-> to15Stage (9-15)")
                to15Stage(bot)
            }
        }
        bot.user().let { user ->
            if (user.levelGte(15) && !bot.inventory().isEquipped(35921)) {
                log("controller", "-> equipmentTopNg (no 35921)")
                equipmentTopNg(bot)
            }
        }
        bot.user().let { user ->
            if (user.levelGte(15) && user.levelLte(levelSpider)
                && !bot.inventory().hasItemById(ITEM_SPIDER_REWARD)
            ) {
                log("controller", "-> pathSpider (15-$levelSpider, no item $ITEM_SPIDER_REWARD)")
                pathSpider(bot)
            }
        }
        bot.user().let { user ->
            if (user.levelGte(15) && user.levelLte(20)
                && bot.inventory().hasItemById(ITEM_SPIDER_REWARD)
            ) {
                log("controller", "-> runExp (15-20, has item $ITEM_SPIDER_REWARD)")
                runExp(bot)
            }
        }
        bot.user().let { user ->
            if (user.levelGte(20) && !bot.inventory().hasItemById(ITEM_CRYPTS_REWARD)) {
                log("controller", "-> runCrypts (20+, no item $ITEM_CRYPTS_REWARD)")
                runCrypts(bot)
            }
        }
        bot.user().let { user ->
            if (user.levelGte(20) && bot.questStatus(QUEST_PROFF) != 3L) {
                log("controller", "-> runQuest (20+, q$QUEST_PROFF != 3)")
                runQuest(bot)
            }
        }

        if (bot.isRangeNpc(GludioVillage.npcManuel, 500) && bot.questStatus(QUEST_PROFF) == 3L) {
            log("controller", "-> relogControl (at Gludio, q$QUEST_PROFF=3)")
            relogControl(bot)
            delay(10000)
        }
        delay(1000)
    }
}

// ==================== Entry point ====================

suspend fun scriptRun(bot: L2Bot) {
    bot.gps.loadBase(GPS_DB)
    log("goldScript", "GPS loaded, starting controller")
    controller(bot)


}
