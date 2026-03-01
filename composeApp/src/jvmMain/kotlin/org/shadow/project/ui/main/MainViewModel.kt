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
import org.shadow.project.logging.LogController
import org.shadow.project.plugin.PluginInfo
import org.shadow.project.plugin.PluginManager
import org.shadow.project.ui.main.model.BotRunInfo
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
                stagedPlugins.map { plugin ->
                    // Collect all running entries for this plugin across all bots
                    val runningEntries = activePlugins.filter { it.key.pluginId == plugin.id }
                    val runningBots = runningEntries.map { (key, entry) ->
                        BotRunInfo(
                            botCharName = key.botCharName,
                            color = botColorMap[key.botCharName] ?: BotColorPalette[0],
                            isRunning = entry.job.isActive
                        )
                    }.filter { it.isRunning }

                    val isSelectedBotRunning = selectedBot?.let { bot ->
                        pluginManager.isRunning(plugin.id, bot)
                    } ?: false

                    PluginsUi(
                        name = plugin.name,
                        active = isSelectedBotRunning,
                        pluginInfo = plugin,
                        details = plugin.description,
                        id = plugin.id,
                        folderName = plugin.folderName,
                        runningBots = runningBots
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
            is MainBotScreenIntent.StopPluginOnBot -> stopPluginOnBot(intent.pluginId, intent.botCharName)
            is MainBotScreenIntent.RemoveActivePlugin -> removeActivePlugin(intent.plugin)
            is MainBotScreenIntent.StopAllPlugins -> stopAllPlugins()
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
        _state.update { state ->
            if (state.stagedPlugins.any { it.id == plugin.id }) state
            else state.copy(stagedPlugins = state.stagedPlugins + plugin)
        }
    }

    // Library: add plugin to active list AND run it immediately
    private fun runPluginFromLibrary(plugin: PluginInfo) {
        addPluginToActive(plugin)
        state.value.selectedBot?.let { bot ->
            pluginManager.runPlugin(plugin, bot, viewModelScope)
        }
    }

    // Active list: toggle run/stop on currently selected bot
    private fun togglePluginOnSelectedBot(pluginUi: PluginsUi) {
        state.value.selectedBot?.let { bot ->
            if (pluginManager.isRunning(pluginUi.pluginInfo.id, bot)) {
                pluginManager.stopPlugin(pluginUi.pluginInfo.id, bot)
            } else {
                pluginManager.runPlugin(pluginUi.pluginInfo, bot, viewModelScope)
            }
        }
    }

    // Stop plugin on a specific bot (from chip click)
    private fun stopPluginOnBot(pluginId: String, botCharName: String) {
        val bot = state.value.bots.find { it.charName == botCharName } ?: return
        pluginManager.stopPlugin(pluginId, bot)
    }

    // Active list: remove from list (stop on ALL bots)
    private fun removeActivePlugin(pluginUi: PluginsUi) {
        pluginManager.stopPluginOnAllBots(pluginUi.pluginInfo.id)
        _state.update { state ->
            state.copy(stagedPlugins = state.stagedPlugins.filter { it.id != pluginUi.pluginInfo.id })
        }
    }

    // Active list: stop all running plugins on all bots
    private fun stopAllPlugins() {
        pluginManager.stopAll()
    }
}
