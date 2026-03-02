package org.shadow.project.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2bot.bridge.api.L2Adrenaline
import com.l2bot.bridge.api.L2Bot
import com.l2bot.bridge.models.events.ConnectionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.shadow.kdrainpluginapi.ConfigField
import org.shadow.kdrainpluginapi.ConfigurablePlugin
import org.shadow.project.logging.LogController
import org.shadow.project.plugin.ConfigStorage
import org.shadow.project.plugin.PluginInfo
import org.shadow.project.plugin.PluginManager
import org.shadow.project.plugin.StagedPlugin
import org.shadow.project.ui.main.model.BotRunInfo
import org.shadow.project.ui.main.model.ConfigDialogState
import org.shadow.project.ui.main.model.MainBotScreenIntent
import org.shadow.project.ui.main.model.MainBotStateIntent
import org.shadow.project.ui.main.model.PluginsUi
import org.shadow.project.ui.theme.BotColorPalette

class MainViewModel(
    private val l2Adrenaline: L2Adrenaline,
    private val logController: LogController,
    private val pluginManager: PluginManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainBotStateIntent())
    val state = _state.asStateFlow()

    // Per-bot pipe drain jobs — keep pipes alive for ALL connected bots
    private val botCollectorJobs = ConcurrentHashMap<String, Job>()

    // User polling — only for the selected bot
    private var userPollingJob: Job? = null

    private val _intents = Channel<MainBotScreenIntent>(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            _intents.receiveAsFlow().collect { intent ->
                handleIntent(intent)
            }
        }
        _intents.trySend(MainBotScreenIntent.LoadBots)

        viewModelScope.launch {
            pluginManager.plugins.collect { plugins ->
                _state.update { it.copy(plugins = plugins) }
            }
        }

        // Derive activePluginsUi from stagedPlugins + running status across all bots
        viewModelScope.launch {
            combine(
                pluginManager.activePlugins,
                _state.map { it.selectedBot }.distinctUntilChanged(),
                _state.map { it.stagedPlugins }.distinctUntilChanged(),
                _state.map { it.botColorMap }.distinctUntilChanged()
            ) { activePlugins, selectedBot, stagedPlugins, botColorMap ->
                stagedPlugins.map { staged ->
                    val plugin = staged.pluginInfo
                    // Filter by BOTH pluginId AND configId
                    val runningEntries = activePlugins.filter {
                        it.key.pluginId == plugin.id && it.key.configId == staged.configId
                    }
                    val runningBots = runningEntries.map { (key, entry) ->
                        BotRunInfo(
                            botCharName = key.botCharName,
                            color = botColorMap[key.botCharName] ?: BotColorPalette[0],
                            isRunning = entry.job.isActive
                        )
                    }.filter { it.isRunning }

                    val isSelectedBotRunning = selectedBot?.let { bot ->
                        pluginManager.isRunning(plugin.id, staged.configId, bot)
                    } ?: false

                    PluginsUi(
                        name = plugin.name,
                        active = isSelectedBotRunning,
                        pluginInfo = plugin,
                        details = plugin.description,
                        id = staged.stageKey,
                        folderName = plugin.folderName,
                        runningBots = runningBots,
                        configLabel = staged.configLabel.ifBlank { null },
                        configId = staged.configId,
                        stagedPlugin = staged
                    )
                }
            }.collect { activePluginsUi ->
                _state.update { it.copy(activePluginsUi = activePluginsUi) }
            }
        }

        // Derive libraryPluginsUi from plugins + searchPlugin
        viewModelScope.launch {
            combine(
                _state.map { it.plugins }.distinctUntilChanged(),
                _state.map { it.searchPlugin }.distinctUntilChanged()
            ) { plugins, searchQuery ->
                plugins
                    .filter { plugin ->
                        searchQuery.isBlank() ||
                                plugin.name.contains(searchQuery, ignoreCase = true) ||
                                plugin.folderName?.contains(searchQuery, ignoreCase = true) == true
                    }
                    .map { plugin ->
                        PluginsUi(
                            name = plugin.name,
                            active = false,
                            pluginInfo = plugin,
                            details = plugin.description,
                            id = plugin.id,
                            folderName = plugin.folderName
                        )
                    }
            }.collect { libraryPluginsUi ->
                _state.update { it.copy(libraryPluginsUi = libraryPluginsUi) }
            }
        }

        _intents.trySend(MainBotScreenIntent.LoadPlugins)
    }

    override fun onCleared() {
        super.onCleared()
        botCollectorJobs.values.forEach { it.cancel() }
        botCollectorJobs.clear()
        userPollingJob?.cancel()
        userPollingJob = null
        _intents.close()
    }

    /**
     * Start pipe-drain collectors for a bot. Keeps action/packet/cliPacket pipes alive
     * so the external process doesn't block. Logs events only when this bot is selected.
     * Safe to call multiple times — skips if already running.
     */
    private fun ensureBotCollectors(bot: L2Bot) {
        if (botCollectorJobs[bot.charName]?.isActive == true) return

        botCollectorJobs[bot.charName] = viewModelScope.launch {
            supervisorScope {
                launch(Dispatchers.IO) {
                    try {
                        bot.errors.collectLatest {
                            logController.logError(bot.charName, it.message ?: "Unknown")
                        }
                    } catch (_: CancellationException) { throw CancellationException()
                    } catch (e: Exception) {
                        logController.logError(bot.charName, "Error collector failed: ${e.message}")
                    }
                }

                launch(Dispatchers.IO) {
                    try {
                        bot.actionEvents.collectLatest {
                            logController.logAction(bot.charName, "${it.action.name} ${it.p1} ${it.p2}")
                        }
                    } catch (_: CancellationException) { throw CancellationException()
                    } catch (e: Exception) {
                        logController.logError(bot.charName, "Action collector failed: ${e.message}")
                    }
                }

                launch(Dispatchers.IO) {
                    try {
                        bot.packetEvents.collectLatest {
                            logController.logServerClient(bot.charName, "${it.header} ${it.data}")
                        }
                    } catch (_: CancellationException) { throw CancellationException()
                    } catch (e: Exception) {
                        logController.logError(bot.charName, "Packet collector failed: ${e.message}")
                    }
                }

                launch(Dispatchers.IO) {
                    try {
                        bot.cliPacketEvents.collectLatest {
                            logController.logClientServer(bot.charName, "${it.header} ${it.data}")
                        }
                    } catch (_: CancellationException) { throw CancellationException()
                    } catch (e: Exception) {
                        logController.logError(bot.charName, "CliPacket collector failed: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Stop pipe-drain collectors for a specific bot (on disconnect).
     */
    private fun stopBotCollectors(bot: L2Bot) {
        botCollectorJobs.remove(bot.charName)?.cancel()
    }

    /**
     * Start user data polling for the selected bot only.
     * TODO: temporarily disabled bot.user() to avoid transport congestion during plugin work
     */
    private fun startUserPolling(bot: L2Bot) {
        userPollingJob?.cancel()
        userPollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val user = bot.user()
                    _state.update { it.copy(user = user) }
                } catch (_: CancellationException) { throw CancellationException()
                } catch (e: Exception) {
                    logController.logError(bot.charName, "User poll failed: ${e.message}")
                }
                delay(5000L)
            }
        }
    }

    fun processIntent(intent: MainBotScreenIntent) {
        viewModelScope.launch { _intents.send(intent) }
    }

    private fun handleIntent(intent: MainBotScreenIntent) {
        when (intent) {
            is MainBotScreenIntent.LoadBots -> loadBots()
            is MainBotScreenIntent.SelectBot -> selectBot(intent.bot)
            is MainBotScreenIntent.ToggleConnection -> toggleConnection()
            is MainBotScreenIntent.ToggleBotConnection -> toggleBotConnection(intent.bot)
            is MainBotScreenIntent.LoadPlugins -> loadPlugins()
            is MainBotScreenIntent.SearchPlugin -> _state.update { it.copy(searchPlugin = intent.query) }
            is MainBotScreenIntent.AddPluginToActive -> addPluginToActive(intent.plugin)
            is MainBotScreenIntent.RunPluginFromLibrary -> runPluginFromLibrary(intent.plugin)
            is MainBotScreenIntent.TogglePluginOnSelectedBot -> togglePluginOnSelectedBot(intent.plugin)
            is MainBotScreenIntent.StopPluginOnBot -> stopPluginOnBot(intent.pluginId, intent.configId, intent.botCharName)
            is MainBotScreenIntent.RemoveActivePlugin -> removeActivePlugin(intent.plugin)
            is MainBotScreenIntent.StopAllPlugins -> stopAllPlugins()
            is MainBotScreenIntent.EditConfig -> editConfig(intent.plugin)
            is MainBotScreenIntent.UpdateConfigDialogValue -> updateConfigDialogValue(intent.key, intent.value)
            is MainBotScreenIntent.UpdateConfigDialogLabel -> updateConfigDialogLabel(intent.label)
            is MainBotScreenIntent.CopyConfigFrom -> copyConfigFrom(intent.stagedPlugin)
            is MainBotScreenIntent.ConfirmConfigDialog -> confirmConfigDialog()
            is MainBotScreenIntent.DismissConfigDialog -> dismissConfigDialog()
        }
    }

    private fun loadBots() {
        viewModelScope.launch {
            val bots = l2Adrenaline.getAvailableBots()
            val existingColorMap = _state.value.botColorMap.toMutableMap()
            var colorIndex = existingColorMap.size
            for (bot in bots) {
                if (bot.charName !in existingColorMap) {
                    existingColorMap[bot.charName] = BotColorPalette[colorIndex % BotColorPalette.size]
                    colorIndex++
                }
            }
            _state.update {
                it.copy(bots = bots, botColorMap = existingColorMap)
            }
        }
    }

    private fun selectBot(bot: L2Bot) {
        _state.update { it.copy(selectedBot = bot) }
        if (bot.connectionStatus.value == ConnectionStatus.CONNECTED) {
            ensureBotCollectors(bot)
            startUserPolling(bot)
        } else {
            userPollingJob?.cancel()
            userPollingJob = null
            _state.update { it.copy(user = null) }
        }
    }

    private fun toggleConnection() {
        val bot = state.value.selectedBot ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (bot.connectionStatus.value == ConnectionStatus.DISCONNECTED) {
                bot.connect()
                ensureBotCollectors(bot)
                startUserPolling(bot)
            } else {
                bot.disconnect()
                stopBotCollectors(bot)
                userPollingJob?.cancel()
                userPollingJob = null
            }
        }
    }

    private fun toggleBotConnection(bot: L2Bot) {
        viewModelScope.launch(Dispatchers.IO) {
            if (bot.connectionStatus.value == ConnectionStatus.DISCONNECTED) {
                bot.connect()
                ensureBotCollectors(bot)
                if (state.value.selectedBot == bot) {
                    startUserPolling(bot)
                }
            } else {
                bot.disconnect()
                stopBotCollectors(bot)
                if (state.value.selectedBot == bot) {
                    userPollingJob?.cancel()
                    userPollingJob = null
                }
            }
        }
    }

    private fun loadPlugins() {
        viewModelScope.launch(Dispatchers.IO) {
            pluginManager.loadPlugins()
        }
    }

    // Library: add plugin to active list (without running)
    private fun addPluginToActive(plugin: PluginInfo) {
        val testInstance = plugin.createInstance()
        if (testInstance is ConfigurablePlugin) {
            openConfigDialog(plugin, runAfterAdd = false)
        } else {
            _state.update { state ->
                if (state.stagedPlugins.any { it.pluginInfo.id == plugin.id && !it.isConfigurable }) state
                else state.copy(
                    stagedPlugins = state.stagedPlugins + StagedPlugin(
                        pluginInfo = plugin,
                        isConfigurable = false
                    )
                )
            }
        }
    }

    // Library: add plugin to active list AND run it immediately
    private fun runPluginFromLibrary(plugin: PluginInfo) {
        val testInstance = plugin.createInstance()
        if (testInstance is ConfigurablePlugin) {
            openConfigDialog(plugin, runAfterAdd = true)
        } else {
            val staged = StagedPlugin(pluginInfo = plugin, isConfigurable = false)
            _state.update { state ->
                if (state.stagedPlugins.any { it.pluginInfo.id == plugin.id && !it.isConfigurable }) state
                else state.copy(stagedPlugins = state.stagedPlugins + staged)
            }
            state.value.selectedBot?.let { bot ->
                val existing = _state.value.stagedPlugins.find {
                    it.pluginInfo.id == plugin.id && !it.isConfigurable
                }
                if (existing != null) {
                    pluginManager.runPlugin(existing, bot, viewModelScope)
                }
            }
        }
    }

    // Active list: toggle run/stop on currently selected bot
    private fun togglePluginOnSelectedBot(pluginUi: PluginsUi) {
        val staged = pluginUi.stagedPlugin ?: return
        state.value.selectedBot?.let { bot ->
            if (pluginManager.isRunning(staged.pluginInfo.id, staged.configId, bot)) {
                pluginManager.stopPlugin(staged.pluginInfo.id, staged.configId, bot)
            } else {
                pluginManager.runPlugin(staged, bot, viewModelScope)
            }
        }
    }

    // Stop plugin on a specific bot (from chip click)
    private fun stopPluginOnBot(pluginId: String, configId: String, botCharName: String) {
        val bot = state.value.bots.find { it.charName == botCharName } ?: return
        pluginManager.stopPlugin(pluginId, configId, bot)
    }

    // Active list: remove from list (stop on ALL bots)
    private fun removeActivePlugin(pluginUi: PluginsUi) {
        val staged = pluginUi.stagedPlugin ?: return
        pluginManager.stopPluginOnAllBots(staged.pluginInfo.id, staged.configId)
        _state.update { state ->
            state.copy(stagedPlugins = state.stagedPlugins.filter { it.stageKey != staged.stageKey })
        }
    }

    // Active list: stop all running plugins on all bots
    private fun stopAllPlugins() {
        pluginManager.stopAll()
    }

    // --- Config Dialog ---

    private fun openConfigDialog(plugin: PluginInfo, runAfterAdd: Boolean) {
        val instance = plugin.createInstance() as? ConfigurablePlugin ?: return
        val fields = instance.configFields
        val defaults = fields.associate { field ->
            field.key to when (field) {
                is ConfigField.Int -> field.defaultValue.toString()
                is ConfigField.Text -> field.defaultValue
                is ConfigField.Bool -> field.defaultValue.toString()
                is ConfigField.Decimal -> field.defaultValue.toString()
                is ConfigField.Select -> field.defaultValue
            }
        }
        val existingConfigs = _state.value.stagedPlugins.filter {
            it.pluginInfo.id == plugin.id && it.isConfigurable
        }
        _state.update {
            it.copy(configDialogState = ConfigDialogState(
                pluginInfo = plugin,
                fields = fields,
                values = defaults,
                configLabel = "",
                runAfterAdd = runAfterAdd,
                existingConfigs = existingConfigs
            ))
        }
    }

    private fun editConfig(pluginUi: PluginsUi) {
        val staged = pluginUi.stagedPlugin ?: return
        if (!staged.isConfigurable) return
        val instance = staged.pluginInfo.createInstance() as? ConfigurablePlugin ?: return

        _state.update {
            it.copy(configDialogState = ConfigDialogState(
                pluginInfo = staged.pluginInfo,
                fields = instance.configFields,
                values = staged.configValues,
                configLabel = staged.configLabel,
                isEditing = true,
                existingConfigId = staged.configId
            ))
        }
    }

    private fun updateConfigDialogValue(key: String, value: String) {
        _state.update { state ->
            val dialog = state.configDialogState ?: return@update state
            state.copy(configDialogState = dialog.copy(values = dialog.values + (key to value)))
        }
    }

    private fun updateConfigDialogLabel(label: String) {
        _state.update { state ->
            val dialog = state.configDialogState ?: return@update state
            state.copy(configDialogState = dialog.copy(configLabel = label))
        }
    }

    private fun copyConfigFrom(source: StagedPlugin) {
        _state.update { state ->
            val dialog = state.configDialogState ?: return@update state
            state.copy(configDialogState = dialog.copy(
                values = source.configValues,
                configLabel = "${source.configLabel} (copy)"
            ))
        }
    }

    private fun confirmConfigDialog() {
        val dialog = _state.value.configDialogState ?: return

        if (dialog.isEditing && dialog.existingConfigId != null) {
            val configId = dialog.existingConfigId
            pluginManager.stopPluginOnAllBots(dialog.pluginInfo.id, configId)

            _state.update { state ->
                state.copy(
                    stagedPlugins = state.stagedPlugins.map { staged ->
                        if (staged.configId == configId) {
                            staged.copy(
                                configValues = dialog.values,
                                configLabel = dialog.configLabel
                            )
                        } else staged
                    },
                    configDialogState = null
                )
            }
            ConfigStorage.save(dialog.pluginInfo.id, configId, dialog.values)
        } else {
            val staged = StagedPlugin(
                pluginInfo = dialog.pluginInfo,
                configLabel = dialog.configLabel.ifBlank { "Config ${System.currentTimeMillis() % 10000}" },
                configValues = dialog.values,
                isConfigurable = true
            )

            _state.update { state ->
                state.copy(
                    stagedPlugins = state.stagedPlugins + staged,
                    configDialogState = null
                )
            }
            ConfigStorage.save(dialog.pluginInfo.id, staged.configId, dialog.values)

            if (dialog.runAfterAdd) {
                state.value.selectedBot?.let { bot ->
                    pluginManager.runPlugin(staged, bot, viewModelScope)
                }
            }
        }
    }

    private fun dismissConfigDialog() {
        _state.update { it.copy(configDialogState = null) }
    }
}
