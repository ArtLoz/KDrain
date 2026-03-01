package com.l2bot.bridge.api

import com.l2bot.bridge.models.L2GPSPoint
import com.l2bot.bridge.models.dialogs.L2ConfirmDlg
import com.l2bot.bridge.models.entities.L2Char
import com.l2bot.bridge.models.entities.L2Live
import com.l2bot.bridge.models.entities.L2Npc
import com.l2bot.bridge.models.entities.L2Pet
import com.l2bot.bridge.models.entities.L2Spawn
import com.l2bot.bridge.models.entities.L2User
import com.l2bot.bridge.models.events.ActionEvent
import com.l2bot.bridge.models.events.CliPacketEvent
import com.l2bot.bridge.models.events.ConnectionStatus
import com.l2bot.bridge.models.events.PacketEvent
import com.l2bot.bridge.models.item.L2Drop
import com.l2bot.bridge.models.item.L2Item
import com.l2bot.bridge.models.skill.L2Skill
import com.l2bot.bridge.models.types.ChatType
import com.l2bot.bridge.models.types.L2Action
import com.l2bot.bridge.models.types.L2Status
import com.l2bot.bridge.models.types.LootType
import com.l2bot.bridge.models.types.RestartType
import com.l2bot.bridge.models.types.WaitResult
import com.l2bot.bridge.models.types.ZoneType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Base controller interface for L2 bot operations.
 * Базовый интерфейс контроллера для операций L2 бота.
 */
interface L2Control {

    /**
     * Returns L2Control that routes all commands to another bot by name.
     * Возвращает L2Control, который перенаправляет все команды другому боту по имени.
     *
     * @param target Character name or "*" for all / Имя персонажа или "*" для всех
     */
    fun on(target: String): L2Control

    // ==================== Data Retrieval / Получение данных ====================

    /**
     * Get user character information.
     * Получить информацию о персонаже пользователя.
     */
    suspend fun user(): L2User

    /**
     * Echo test - returns the sent text back.
     * Тест эхо - возвращает отправленный текст обратно.
     */
    suspend fun echo(text: String): String

    /**
     * Get list of NPCs around character.
     * Получить список НПЦ вокруг персонажа.
     */
    suspend fun npcList(): List<L2Npc>

    /**
     * Get list of user pets and summons.
     * Получить список питомцев и саммонов пользователя.
     */
    suspend fun petList(): List<L2Pet>

    /**
     * Get inventory of user character.
     * Получить инвентарь персонажа пользователя.
     */
    suspend fun inventory(): List<L2Item>

    /**
     * Get Quest inventory of user character.
     * Получить квест инвентарь персонажа пользователя.
     */

    suspend fun questInventory(): List<L2Item>

    /**
     * Get character skills list.
     * Получить список умений персонажа.
     */
    suspend fun skillList(): List<L2Skill>

    /**
     * Get list of players around character.
     * Получить список игроков вокруг персонажа.
     */
    suspend fun charList(): List<L2Char>

    /**
     * Get list of drop/items laying on the ground.
     * Получить список дропа/предметов на земле.
     */
    suspend fun dropList(): List<L2Drop>

    // ==================== Movement / Перемещение ====================

    /**
     * Move to point (x, y, z).
     * Передвинуться к точке (x, y, z).
     *
     * @param x X coordinate / X координата
     * @param y Y coordinate / Y координата
     * @param z Z coordinate / Z координата
     * @param timeout Movement timeout in ms / Таймаут движения в мс
     */
    suspend fun moveTo(x: Int, y: Int, z: Int, timeout: Int = 8000): Boolean

    /**
     * Move to object.
     * Передвинуться к объекту.
     *
     * @param obj Target object / Целевой объект
     * @param delta Distance offset / Смещение дистанции
     */
    suspend fun moveTo(obj: L2Spawn, delta: Int = -70): Boolean

    /**
     * Move to current target.
     * Передвинуться к текущей цели.
     *
     * @param delta Distance offset / Смещение дистанции
     */
    suspend fun moveToTarget(delta: Int = -100): Boolean

    /**
     * Use return to village (/unstuck).
     * Использовать возврат в город (/unstuck).
     */
    suspend fun unstuck(): Boolean

    /**
     * Resurrect (to village / to clan hall / ...).
     * Воскреснуть (в город / в холл клана / ...).
     *
     * @param type Restart type / Тип воскрешения
     */
    suspend fun goHome(type: RestartType = RestartType.TOWN): Boolean

    /**
     * Teleport to location. For new game chronicles.
     * Телепортироваться в локацию. Для новых хроник.
     *
     * @param id Teleport location ID / ID локации телепорта
     */
    suspend fun teleport(id: Long): Boolean

    // ==================== Actions / Действия ====================

    /**
     * Use action (from action bar).
     * Использовать действие (из панели действий).
     *
     * @param id Action ID / ID действия
     * @param force Force attack / Принудительная атака
     * @param shift Shift modifier / Модификатор Shift
     */
    suspend fun useAction(id: Long, force: Boolean = false, shift: Boolean = false): Boolean

    /**
     * Attack target.
     * Атаковать цель.
     *
     * @param pauseTime Pause time between attacks in ms / Пауза между атаками в мс
     * @param force Force attack / Принудительная атака
     */
    suspend fun attack(pauseTime: Long = 2000L, force: Boolean = false): Boolean

    /**
     * Pickup drop/item laying on the ground.
     * Подобрать дроп/предмет с земли.
     *
     * @param item Item to pickup / Предмет для подбора
     * @param byPet Pickup by pet / Подобрать питомцем
     */
    suspend fun pickUp(item: L2Drop, byPet: Boolean = false): Boolean

    /**
     * Stand up.
     * Встать.
     */
    suspend fun stand(): Boolean

    // ==================== Targeting / Нацеливание ====================

    /**
     * Target object by name.
     * Нацелиться на объект по имени.
     *
     * @param name Target name / Имя цели
     */
    suspend fun setTarget(name: String): Boolean

    /**
     * Target object by ID.
     * Нацелиться на объект по ID.
     *
     * @param id Target ID / ID цели
     */
    suspend fun setTargetId(id: Long): Boolean

    /**
     * Target object.
     * Нацелиться на объект.
     *
     * @param obj Target object / Целевой объект
     */
    suspend fun setTarget(obj: L2Live): Boolean

    /**
     * Target object. Without waiting for server answer.
     * Нацелиться на объект. Без ожидания ответа сервера.
     *
     * @param oid Object ID / ID объекта
     * @param force Force action / Принудительное действие
     */
    suspend fun action(oid: Long, force: Boolean = false): Boolean

    /**
     * Cancel target.
     * Сбросить цель.
     */
    suspend fun cancelTarget(): Boolean

    /**
     * Target another object's target (assist).
     * Нацелиться на цель другого объекта (ассист).
     *
     * @param name Player name to assist / Имя игрока для ассиста
     */
    suspend fun assist(name: String): Boolean

    /**
     * Search attacking enemy of object.
     * Найти атакующего врага объекта.
     *
     * @param obj Object to search enemy for / Объект для поиска врага
     * @param range Search range / Радиус поиска
     * @param zLimit Z-axis limit / Ограничение по оси Z
     */
    suspend fun findEnemy(obj: L2Live, range: Long = 2000L, zLimit: Long = 300L): L2Live?

    /**
     * Activate automatic targeting.
     * Активировать автоматический поиск целей.
     *
     * @param range Search range / Радиус поиска
     * @param zLimit Z-axis limit / Ограничение по оси Z
     * @param notBusy Only not busy targets / Только свободные цели
     */
    suspend fun autoTarget(range: Long = 2000L, zLimit: Long = 300L, notBusy: Boolean = true): Boolean

    /**
     * Add object to bot ignoring list.
     * Добавить объект в список игнорируемых ботом.
     *
     * @param obj Object to ignore / Объект для игнорирования
     */
    suspend fun ignore(obj: L2Spawn)

    /**
     * Clear list of ignored objects.
     * Очистить список игнорируемых объектов.
     */
    suspend fun clearIgnore()

    /**
     * Check if NPC is already attacked by another player.
     * Проверить, атакуется ли НПЦ другим игроком.
     *
     * @param obj NPC to check / НПЦ для проверки
     */
    suspend fun isBusy(obj: L2Npc): Boolean

    // ==================== Skills / Умения ====================

    /**
     * Use skill.
     * Использовать умение.
     *
     * @param id Skill ID / ID умения
     * @param force Force cast / Принудительное использование
     * @param shift Shift modifier / Модификатор Shift
     */
    suspend fun useSkill(id: Long, force: Boolean = false, shift: Boolean = false): Boolean

    /**
     * Use skill. Without waiting for cast ending and server answer.
     * Использовать умение. Без ожидания окончания каста и ответа сервера.
     *
     * @param id Skill ID / ID умения
     * @param force Force cast / Принудительное использование
     * @param shift Shift modifier / Модификатор Shift
     */
    suspend fun dUseSkill(id: Long, force: Boolean = false, shift: Boolean = false): Boolean

    /**
     * Use skill on ground (AoE).
     * Использовать умение на площадь (AoE).
     *
     * @param id Skill ID / ID умения
     * @param x X coordinate / X координата
     * @param y Y coordinate / Y координата
     * @param z Z coordinate / Z координата
     * @param force Force cast / Принудительное использование
     * @param shift Shift modifier / Модификатор Shift
     */
    suspend fun useSkillGround(id: Long, x: Int, y: Int, z: Int, force: Boolean = false, shift: Boolean = false): Boolean

    /**
     * Interrupt skill cast.
     * Прервать использование умения.
     */
    suspend fun stopCasting(): Boolean

    /**
     * Dispel buff.
     * Отменить бафф.
     *
     * @param id Buff ID to dispel / ID баффа для отмены
     */
    suspend fun dispel(id: Long): Boolean

    /**
     * Learn skill.
     * Изучить умение.
     *
     * @param id Skill ID to learn / ID умения для изучения
     */
    suspend fun learnSkill(id: Long): Boolean

    /**
     * Update skill list.
     * Обновить список умений.
     */
    suspend fun updateSkillList(): Boolean

    // ==================== Items / Предметы ====================

    /**
     * Use item by object.
     * Использовать предмет по объекту.
     *
     * @param obj Item object / Объект предмета
     * @param byPet Use by pet / Использовать питомцем
     * @param force Force use / Принудительное использование
     */
    suspend fun useItem(obj: L2Item, byPet: Boolean = false, force: Boolean = false): Boolean

    /**
     * Use item by name.
     * Использовать предмет по имени.
     *
     * @param name Item name / Имя предмета
     * @param byPet Use by pet / Использовать питомцем
     * @param force Force use / Принудительное использование
     */
    suspend fun useItem(name: String, byPet: Boolean = false, force: Boolean = false): Boolean

    /**
     * Use item by ID.
     * Использовать предмет по ID.
     *
     * @param id Item ID / ID предмета
     * @param byPet Use by pet / Использовать питомцем
     * @param force Force use / Принудительное использование
     */
    suspend fun useItem(id: Long, byPet: Boolean = false, force: Boolean = false): Boolean

    /**
     * Use item by unique identifier (OID).
     * Использовать предмет по уникальному идентификатору (OID).
     *
     * @param oid Item OID / OID предмета
     * @param byPet Use by pet / Использовать питомцем
     * @param force Force use / Принудительное использование
     */
    suspend fun useItemOid(oid: Long, byPet: Boolean = false, force: Boolean = false): Boolean

    /**
     * Delete item by name.
     * Удалить предмет по имени.
     *
     * @param name Item name / Имя предмета
     * @param count Amount to delete / Количество для удаления
     */
    suspend fun destroyItem(name: String, count: Long): Boolean

    /**
     * Delete item by ID.
     * Удалить предмет по ID.
     *
     * @param id Item ID / ID предмета
     * @param count Amount to delete / Количество для удаления
     */
    suspend fun destroyItem(id: Int, count: Long): Boolean

    /**
     * Delete item by object.
     * Удалить предмет по объекту.
     *
     * @param item Item object / Объект предмета
     * @param count Amount to delete / Количество для удаления
     */
    suspend fun destroyItem(item: L2Item, count: Long): Boolean

    /**
     * Drop item on ground.
     * Выбросить предмет на землю.
     *
     * @param id Item ID / ID предмета
     * @param count Amount to drop / Количество для выброса
     * @param x X coordinate / X координата
     * @param y Y coordinate / Y координата
     * @param z Z coordinate / Z координата
     */
    suspend fun dropItem(id: Long, count: Long, x: Int, y: Int, z: Int): Boolean

    /**
     * Craft item.
     * Создать/скрафтить предмет.
     *
     * @param index Recipe index / Индекс рецепта
     */
    suspend fun makeItem(index: Long): Boolean

    /**
     * Crystallize item by ID.
     * Кристаллизовать предмет по ID.
     *
     * @param id Item ID / ID предмета
     */
    suspend fun crystalItem(id: Long): Boolean

    /**
     * Crystallize item by object.
     * Кристаллизовать предмет по объекту.
     *
     * @param item Item object / Объект предмета
     */
    suspend fun crystalItem(item: L2Item): Boolean

    /**
     * Move item between user and pet.
     * Передать предмет между пользователем и питомцем.
     *
     * @param name Item name / Имя предмета
     * @param count Amount to move / Количество для передачи
     * @param toPet Move to pet (true) or from pet (false) / Передать питомцу (true) или от питомца (false)
     */
    suspend fun moveItem(name: String, count: Long, toPet: Boolean): Boolean

    /**
     * Move items between user and warehouse.
     * Передать предметы между пользователем и складом.
     *
     * @param toWh Move to warehouse (true) or from warehouse (false) / Передать на склад (true) или со склада (false)
     * @param ids List of item IDs / Список ID предметов
     */
    suspend fun loadItems(toWh: Boolean, ids: List<Long>): Boolean

    /**
     * Enable/disable automatic soulshots.
     * Активировать/деактивировать автоматическое использование зарядов.
     *
     * @param name Soulshot name / Название заряда
     * @param active Enable (true) or disable (false) / Включить (true) или выключить (false)
     */
    suspend fun autoSoulShot(name: String, active: Boolean): Boolean

    /**
     * Enable/disable automatic soulshots. Without waiting.
     * Активировать/деактивировать автозаряды. Без ожидания.
     *
     * @param id Soulshot ID / ID заряда
     * @param active Enable (true) or disable (false) / Включить (true) или выключить (false)
     */
    suspend fun dAutoSoulShot(id: Long, active: Boolean): Boolean

    /**
     * Get equipped items count by name.
     * Получить количество надетых предметов по имени.
     *
     * @param name Item name / Имя предмета
     */
    suspend fun equipped(name: String): Int

    // ==================== Pets & Summons / Питомцы и саммоны ====================

    /**
     * Dismiss pet.
     * Отозвать питомца.
     */
    suspend fun dismissPet(): Boolean

    /**
     * Dismiss summon.
     * Отозвать слугу/саммона.
     */
    suspend fun dismissSum(): Boolean

    // ==================== Social / Социальное ====================

    /**
     * Write message to game chat.
     * Написать сообщение в игровой чат.
     *
     * @param text Message text / Текст сообщения
     * @param chatType Chat type / Тип чата
     * @param playerName Player name for whisper / Имя игрока для приватного сообщения
     */
    suspend fun say(text: String, chatType: ChatType, playerName: String = ""): Boolean

    /**
     * Invite player to group/party.
     * Пригласить игрока в группу/пати.
     *
     * @param name Player name / Имя игрока
     * @param lootMode Loot distribution mode / Режим распределения добычи
     */
    suspend fun inviteParty(name: String, lootMode: LootType = LootType.LOOTER): Boolean

    /**
     * Dismiss player from group/party.
     * Исключить игрока из группы/пати.
     *
     * @param name Player name / Имя игрока
     */
    suspend fun dismissParty(name: String): Boolean

    /**
     * Answer to group/party invite.
     * Ответить на приглашение в группу/пати.
     *
     * @param accept Accept (true) or decline (false) / Принять (true) или отклонить (false)
     */
    suspend fun joinParty(accept: Boolean): Boolean

    /**
     * Leave from party.
     * Выйти из группы.
     */
    suspend fun leaveParty(): Boolean

    /**
     * Transfer group/party leader status.
     * Передать лидерство группы/пати.
     *
     * @param name New leader name / Имя нового лидера
     */
    suspend fun setPartyLeader(name: String): Boolean

    /**
     * Get mentor name.
     * Получить имя наставника.
     */
    suspend fun getMentor(): String

    /**
     * Leave mentor.
     * Покинуть наставника.
     */
    suspend fun kickMentor(): Boolean

    /**
     * Close group/party room.
     * Закрыть комнату группы/пати.
     */
    suspend fun closeRoom(): Boolean

    /**
     * Create group/party room.
     * Создать комнату группы/пати.
     *
     * @param caption Room caption / Название комнаты
     * @param minLevel Minimum level / Минимальный уровень
     * @param maxLevel Maximum level / Максимальный уровень
     */
    suspend fun createRoom(caption: String, minLevel: Int = 1, maxLevel: Int = 110): Boolean

    /**
     * Activate automatic entry to clan.
     * Активировать автоматическое вступление в клан.
     *
     * @param players List of players to auto-accept / Список игроков для автопринятия
     */
    suspend fun autoAcceptClan(players: List<String>): Boolean

    /**
     * Activate automatic entry to command channel.
     * Активировать автоматическое вступление в командный канал.
     *
     * @param players List of players to auto-accept / Список игроков для автопринятия
     */
    suspend fun autoAcceptCC(players: List<String>): Boolean

    /**
     * Activate automatic accept mentors.
     * Активировать автоматическое соглашение на наставничество.
     *
     * @param players List of mentors to auto-accept / Список наставников для автопринятия
     */
    suspend fun autoAcceptMentors(players: List<String>): Boolean

    // ==================== Quests / Квесты ====================

    /**
     * Get quest stage.
     * Получить этап квеста.
     *
     * @param id Quest ID / ID квеста
     * @return Quest stage / Этап квеста
     */
    suspend fun questStatus(id: Long): Long

    /**
     * Check quest stage.
     * Проверить этап квеста.
     *
     * @param id Quest ID / ID квеста
     * @param stage Stage to check / Этап для проверки
     * @return true if quest is at specified stage / true если квест на указанном этапе
     */
    suspend fun questStatus(id: Long, stage: Long): Boolean

    /**
     * Cancel quest.
     * Отменить квест.
     *
     * @param id Quest ID / ID квеста
     */
    suspend fun cancelQuest(id: Int): Boolean

    /**
     * Opens quest 'question mark'.
     * Открыть квестовый 'знак вопроса'.
     */
    suspend fun openQuestion(): Boolean

    /**
     * Get all daily items/rewards.
     * Получить все ежедневные предметы/награды.
     */
    suspend fun getDailyItems(): Boolean

    /**
     * Get one of the daily rewards.
     * Получить одну из ежедневных наград.
     *
     * @param id Daily reward ID / ID ежедневной награды
     */
    suspend fun getDailyItem(id: Long): Boolean

    /**
     * Update daily rewards list.
     * Обновить список ежедневных наград.
     */
    suspend fun updateDailyList(): Boolean

    // ==================== Dialogs / Диалоги ====================

    /**
     * Open dialog with target (NPC).
     * Открыть диалог с целью (НПЦ).
     *
     * @param timeout Timeout in ms / Таймаут в мс
     */
    suspend fun dlgOpen(timeout: Long = 5000): Boolean

    /**
     * Select dialog option by index.
     * Выбрать пункт диалога по индексу.
     *
     * @param index Option index / Индекс пункта
     */
    suspend fun dlgSel(index: Int): Boolean

    /**
     * Select dialog option by caption/text.
     * Выбрать пункт диалога по тексту.
     *
     * @param caption Option text / Текст пункта
     * @param timeout Timeout in ms / Таймаут в мс
     */
    suspend fun dlgSel(caption: String, timeout: Int = 1000): Boolean

    /**
     * Send command/bypass to server.
     * Отправить команду/bypass на сервер.
     *
     * @param text Bypass text / Текст bypass
     */
    suspend fun bypassToServer(text: String): Boolean

    /**
     * Get common dialog text.
     * Получить текст обычного диалога.
     */
    suspend fun dlgText(): String

    /**
     * Get last common dialog appearing time moment.
     * Получить момент времени появления последнего диалога.
     */
    suspend fun dlgTime(): Long

    /**
     * Get Community Board (Alt+B) dialog text.
     * Получить текст Community Board (Alt+B) диалога.
     */
    suspend fun cbText(): String

    /**
     * Get last Community Board appearing time moment.
     * Получить момент времени появления последнего Community Board.
     */
    suspend fun cbTime(): Long

    /**
     * Get help dialog text.
     * Получить текст диалога помощи.
     */
    suspend fun hlpText(): String

    /**
     * Get last help dialog appearing time moment.
     * Получить момент времени появления последнего диалога помощи.
     */
    suspend fun hlpTime(): Long

    /**
     * Get dialog with accept ability (confirm dialog).
     * Получить диалог с возможностью подтверждения.
     */
    suspend fun getConfirmDlg(): L2ConfirmDlg?

    /**
     * Accept/reject confirm dialog.
     * Подтвердить/отклонить диалог.
     *
     * @param accept Accept (true) or reject (false) / Подтвердить (true) или отклонить (false)
     */
    suspend fun confirmDialog(accept: Boolean): Boolean

    // ==================== Trade / Торговля ====================

    /**
     * Open private store (selling / buying / crafting).
     * Открыть личную торговую лавку (продажа / покупка / крафт).
     *
     * @param items List of item data / Список данных предметов
     * @param storeType Store type / Тип лавки
     * @param caption Store caption / Название лавки
     */
    suspend fun openPrivateStore(items: List<Long>, storeType: Int, caption: String): Boolean

    /**
     * Buy/sell items to NPC.
     * Купить/продать предметы у НПЦ.
     *
     * @param sell Sell (true) or buy (false) / Продать (true) или купить (false)
     * @param items List of item data / Список данных предметов
     */
    suspend fun npcTrade(sell: Boolean, items: List<Long>): Boolean

    /**
     * Exchange items with NPC / dimensional merchant.
     * Обменять предметы у НПЦ / dimensional merchant.
     *
     * @param idOrIndex Item ID or index / ID или индекс предмета
     * @param count Amount to exchange / Количество для обмена
     * @param byIndex Use index instead of ID / Использовать индекс вместо ID
     */
    suspend fun npcExchange(idOrIndex: Long, count: Long = 1, byIndex: Boolean = false): Boolean

    /**
     * Get town tax percentage.
     * Получить процент налога в городе.
     *
     * @param townId Town ID / ID города
     */
    suspend fun castleTax(townId: Long): Int

    // ==================== Mail / Почта ====================

    /**
     * Send mail.
     * Отправить письмо.
     *
     * @param receiver Receiver name / Имя получателя
     * @param topic Mail topic / Тема письма
     * @param text Mail text / Текст письма
     * @param items List of attached items / Список прикрепленных предметов
     * @param price COD price / Цена наложенного платежа
     */
    suspend fun sendMail(receiver: String, topic: String, text: String, items: List<Long> = emptyList(), price: Long = 0): Boolean

    /**
     * Get mail items/attachments.
     * Получить предметы/вложения из писем.
     *
     * @param maxLoad Maximum load weight / Максимальный вес загрузки
     * @param maxCount Maximum item count / Максимальное количество предметов
     */
    suspend fun getMailItems(maxLoad: Long = 65, maxCount: Long = 1000): Boolean

    // ==================== Zone & Time / Зона и время ====================

    /**
     * Get current game zone type.
     * Получить тип текущей игровой зоны.
     */
    suspend fun getZoneType(): ZoneType

    /**
     * Get location name by coordinates.
     * Получить название локации по координатам.
     *
     * @param x X coordinate / X координата
     * @param y Y coordinate / Y координата
     * @param z Z coordinate / Z координата
     */
    suspend fun getZoneName(x: Int, y: Int, z: Int): String

    /**
     * Get location ID by coordinates.
     * Получить ID локации по координатам.
     *
     * @param x X coordinate / X координата
     * @param y Y coordinate / Y координата
     * @param z Z coordinate / Z координата
     */
    suspend fun getZoneID(x: Int, y: Int, z: Int): Long

    /**
     * Check if point is in zone configured on bot map.
     * Проверить, находится ли точка в зоне, настроенной на карте бота.
     *
     * @param x X coordinate / X координата
     * @param y Y coordinate / Y координата
     * @param z Z coordinate / Z координата
     */
    suspend fun inZone(x: Int, y: Int, z: Int): Boolean

    /**
     * Check if object is in zone configured on bot map.
     * Проверить, находится ли объект в зоне, настроенной на карте бота.
     *
     * @param obj Object to check / Объект для проверки
     */
    suspend fun inZone(obj: L2Spawn): Boolean

    /**
     * Get current game time (in minutes).
     * Получить текущее игровое время (в минутах).
     */
    suspend fun gameTime(): Long

    /**
     * Check if it is day time in game.
     * Проверить, является ли время в игре дневным.
     */
    suspend fun isDay(): Boolean

    // ==================== Account Status / Статус аккаунта ====================

    /**
     * Get account status.
     * Получить статус аккаунта.
     */
    suspend fun getStatus(): L2Status

    /**
     * Get account login stage.
     * Получить этап загрузки аккаунта.
     */
    suspend fun loginStatus(): Int

    /**
     * Enter account login and password.
     * Ввести логин и пароль аккаунта.
     *
     * @param login Account login / Логин аккаунта
     * @param password Account password / Пароль аккаунта
     */
    suspend fun authLogin(login: String, password: String): Boolean

    /**
     * Select character on character selection screen.
     * Выбрать персонажа на экране выбора персонажей.
     *
     * @param charIndex Character index (-1 for last used) / Индекс персонажа (-1 для последнего использованного)
     */
    suspend fun gameStart(charIndex: Int = -1): Boolean

    /**
     * Logout to character selecting screen.
     * Выйти на экран выбора персонажей.
     */
    suspend fun restart(): Boolean

    /**
     * Logout to character selecting screen. Without waiting.
     * Выйти на экран выбора персонажей. Без ожидания.
     */
    suspend fun dRestart(): Boolean

    // ==================== Bot Settings / Настройки бота ====================

    /**
     * Activate/deactivate bot settings module (face control).
     * Активировать/деактивировать модуль настроек бота (face control).
     *
     * @param id Module ID / ID модуля
     * @param active Activate (true) or deactivate (false) / Активировать (true) или деактивировать (false)
     */
    suspend fun setFaceControl(id: Int, active: Boolean): Boolean

    /**
     * Get bot settings module state.
     * Получить состояние модуля настроек бота.
     *
     * @param id Module ID / ID модуля
     */
    suspend fun getFaceState(id: Int): Boolean

    /**
     * Update (reread) bot settings.
     * Обновить (перечитать) настройки бота.
     *
     * @param wait Wait for completion / Ожидать завершения
     */
    suspend fun updateCfg(wait: Boolean = true): Boolean

    /**
     * Load bot settings from file.
     * Загрузить настройки бота из файла.
     *
     * @param filePath Path to settings file / Путь к файлу настроек
     */
    suspend fun loadConfig(filePath: String): Boolean

    /**
     * Load bot map zones settings from file.
     * Загрузить настройки зон карты бота из файла.
     *
     * @param filePath Path to zones file / Путь к файлу зон
     */
    suspend fun loadZone(filePath: String): Boolean

    /**
     * Clear zones on bot map.
     * Очистить зоны на карте бота.
     */
    suspend fun clearZone(): Boolean

    /**
     * Set performance level for game client.
     * Установить уровень производительности для игрового клиента.
     *
     * @param level Performance level / Уровень производительности
     */
    suspend fun setPerform(level: Long): Boolean

    /**
     * Change distance that bot will retreat from zone edges.
     * Изменить дистанцию отступа бота от краев зон.
     *
     * @param dist Distance / Дистанция
     */
    suspend fun setMapKeepDist(dist: Int): Boolean

    // ==================== Game Client / Игровой клиент ====================

    /**
     * Write message to game chat. Visible only for user.
     * Написать сообщение в игровой чат. Видно только пользователю.
     *
     * @param text Message text / Текст сообщения
     * @param author Author name / Имя автора
     * @param chatType Chat type / Тип чата
     */
    suspend fun gamePrint(text: String, author: String = "", chatType: Int = 0): Boolean

    /**
     * Close game client.
     * Закрыть игровой клиент.
     */
    suspend fun gameClose(): Boolean

    /**
     * Activate game client / Adrenaline window blinking.
     * Активировать мигание окна игрового клиента / Adrenaline.
     *
     * @param game Blink game window (true) or Adrenaline window (false) / Мигать окном игры (true) или Adrenaline (false)
     */
    suspend fun blinkWindow(game: Boolean = true): Boolean

    /**
     * Show/hide game client window.
     * Показать/скрыть окно игрового клиента.
     *
     * @param show Show (true) or hide (false) / Показать (true) или скрыть (false)
     */
    suspend fun setGameWindow(show: Boolean): Boolean

    /**
     * Press keyboard button in game client window (by key name).
     * Нажать клавишу клавиатуры в окне игрового клиента (по имени клавиши).
     *
     * @param keyName Key name / Имя клавиши
     * @param downUp Down (0), Up (1), or Both (2) / Нажать (0), Отпустить (1), или Оба (2)
     */
    suspend fun useKey(keyName: String, downUp: Int = 0): Boolean

    /**
     * Press keyboard button in game client window (by key code).
     * Нажать клавишу клавиатуры в окне игрового клиента (по коду клавиши).
     *
     * @param keyCode Key code / Код клавиши
     * @param downUp Down (0), Up (1), or Both (2) / Нажать (0), Отпустить (1), или Оба (2)
     */
    suspend fun useKey(keyCode: Int, downUp: Int = 0): Boolean

    /**
     * Type text in game client window.
     * Напечатать текст в окне игрового клиента.
     *
     * @param text Text to type / Текст для ввода
     */
    suspend fun enterText(text: String): Boolean

    /**
     * Send PostMessage to game client window.
     * Отправить PostMessage в окно игрового клиента.
     *
     * @param msg Message ID / ID сообщения
     * @param wParam wParam value / Значение wParam
     * @param lParam lParam value / Значение lParam
     */
    suspend fun postMessage(msg: Long, wParam: Int, lParam: Int): Int

    /**
     * Send SendMessage to game client window.
     * Отправить SendMessage в окно игрового клиента.
     *
     * @param msg Message ID / ID сообщения
     * @param wParam wParam value / Значение wParam
     * @param lParam lParam value / Значение lParam
     */
    suspend fun sendMessage(msg: Long, wParam: Int, lParam: Int): Int

    /**
     * Get path to game client executable file.
     * Получить путь к исполняемому файлу игрового клиента.
     */
    suspend fun getGamePath(): String

    /**
     * Get game client window handle/identifier.
     * Получить идентификатор/хэндл окна игрового клиента.
     */
    suspend fun getGameWindowHandle(): Long

    /**
     * Get game client hash.
     * Получить хеш игрового клиента.
     */
    suspend fun getGameHash(): Long

    /**
     * Get game client protocol version.
     * Получить версию протокола игрового клиента.
     */
    suspend fun getGameProtocol(): Int

    /**
     * Get game client version.
     * Получить версию игрового клиента.
     */
    suspend fun getGameVersion(): Int

    // ==================== Game Server / Игровой сервер ====================

    /**
     * Get game server IP-address.
     * Получить IP-адрес игрового сервера.
     */
    suspend fun getServerIP(): String

    /**
     * Get game server name.
     * Получить название игрового сервера.
     */
    suspend fun getServerName(): String

    /**
     * Get game server ID.
     * Получить ID игрового сервера.
     */
    suspend fun getServerID(): Int

    /**
     * Check if server is classic (L2 Classic).
     * Проверить, является ли сервер классическим (L2 Classic).
     */
    suspend fun isClassicServer(): Boolean

    /**
     * Get current server time (in minutes).
     * Получить текущее время сервера (в минутах).
     */
    suspend fun getServerTime(): Long

    // ==================== Adrenaline Bot Window / Окно Adrenaline ====================

    /**
     * Write message/log in Adrenaline interface.
     * Написать сообщение/лог в интерфейсе Adrenaline.
     *
     * @param title Message title / Заголовок сообщения
     * @param text Message text / Текст сообщения
     * @param color Text color (RGB) / Цвет текста (RGB)
     */
    suspend fun msg(title: String, text: String, color: Int = 0xFFFFFF): Boolean

    /**
     * Activate Adrenaline window blinking.
     * Активировать мигание окна Adrenaline.
     *
     * @param game Blink game window (true) or bot window (false) / Мигать окном игры (true) или бота (false)
     */
    suspend fun blinkWindowBot(game: Boolean = true): Boolean

    /**
     * Activate/deactivate script controlling by hotkey (Ins).
     * Активировать/деактивировать управление скриптом по горячей клавише (Ins).
     *
     * @param enable Enable (true) or disable (false) / Включить (true) или выключить (false)
     */
    suspend fun setScriptPause(enable: Boolean): Boolean

    // ==================== Network / Сеть ====================

    /**
     * Send activity ID to server.
     * Отправить ID активности на сервер.
     *
     * @param level Activity level / Уровень активности
     */
    suspend fun sendActID(level: Long): Boolean

    /**
     * Send raw packet to server.
     * Отправить сырой пакет на сервер.
     *
     * @param text Hex packet data / Hex данные пакета
     */
    suspend fun sendToServer(text: String): Boolean

    /**
     * Send raw packet to client.
     * Отправить сырой пакет клиенту.
     *
     * @param text Hex packet data / Hex данные пакета
     */
    suspend fun sendToClient(text: String): Boolean

    /**
     * Block packet for specified time.
     * Заблокировать пакет на указанное время.
     *
     * @param id Packet ID / ID пакета
     * @param id2 Secondary packet ID / Вторичный ID пакета
     * @param isServerPacket Server packet (true) or client packet (false) / Серверный пакет (true) или клиентский (false)
     * @param timeMs Block time in ms / Время блокировки в мс
     */
    suspend fun blockPacket(id: Int, id2: Int = 0, isServerPacket: Boolean, timeMs: Long = 0xFFFFFFFFL): Boolean

    // ==================== Miscellaneous / Разное ====================

    /**
     * Wait for event/action.
     * Ожидать событие/действие.
     *
     * @param action Action type to wait for / Тип действия для ожидания
     * @param timeout Timeout in ms / Таймаут в мс
     */
    suspend fun waitAction(action: L2Action, timeout: Long = 5000): WaitResult

    // ==================== GPS Navigation / GPS Навигация ====================

    /**
     * Load GPS map from file.
     * Загрузить GPS карту из файла.
     *
     * @param filePath Path to GPS map file (.db3) / Путь к файлу GPS карты (.db3)
     * @return Number of loaded points / Количество загруженных точек
     */
    suspend fun loadGpsMap(filePath: String): Int

    /**
     * Move to GPS point by name.
     * Переместиться к GPS точке по имени.
     *
     * @param gpsPointName GPS point name / Имя GPS точки
     */
    suspend fun moveGpsPoint(gpsPointName: String): Boolean

    /**
     * Get GPS point by name.
     * Получить GPS точку по имени.
     *
     * @param gpsPointName GPS point name / Имя GPS точки
     */
    suspend fun getGpsPoint(gpsPointName: String): L2GPSPoint

    /**
     * Move to GPS point with random offset.
     * Переместиться к GPS точке со случайным смещением.
     *
     * @param gpsPointName GPS point name / Имя GPS точки
     * @param range Random offset range / Диапазон случайного смещения
     */
    suspend fun moveGpsPointRandom(gpsPointName: String, range: Int = 100): Boolean

    /**
     * Get list of engines or available bot instances.
     * Получить список движков или доступных инстансов бота.
     */
    suspend fun getEngineList(refresh: Boolean = false):List<L2User>

    // ==================== Events / События ====================

    /**
     * Flow of action events (combat, movement, etc.).
     * Поток событий действий (бой, движение и т.д.).
     */
    val actionEvents: Flow<ActionEvent>

    /**
     * Flow of server → client packet events.
     * Поток событий пакетов сервер → клиент.
     */
    val packetEvents: Flow<PacketEvent>

    /**
     * Flow of client → server packet events.
     * Поток событий пакетов клиент → сервер.
     */
    val cliPacketEvents: Flow<CliPacketEvent>

    /**
     * Flow of connection status changes.
     * Поток изменений статуса подключения.
     */
    val connectionStatus: StateFlow<ConnectionStatus>
}
