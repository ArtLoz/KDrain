package org.shadow.project.ui.main.old

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2bot.bridge.api.L2Adrenaline
import com.l2bot.bridge.api.L2Bot
import com.l2bot.bridge.models.events.ConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.shadow.project.logging.LogController
import org.shadow.project.plugin.PluginManager
import org.shadow.project.ui.main.model.PluginsUi
import org.shadow.project.ui.main.old.model.MainBotScreenIntent
import org.shadow.project.ui.main.old.model.MainBotStateIntent

class MainViewModel(
    private val l2Adrenaline: L2Adrenaline,
    private val logController: LogController,
    private val pluginManager: PluginManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainBotStateIntent())
    val state = _state.asStateFlow()
    private var botCollectorsJob: Job? = null

    private val _intents = Channel<MainBotScreenIntent>(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            _intents.receiveAsFlow().collect { intent ->
                handleIntent(intent)
            }
        }
        handleIntent(MainBotScreenIntent.LoadBots)

        viewModelScope.launch {
            pluginManager.plugins.collect { plugins ->
                _state.update { it.copy(plugins = plugins) }
            }
        }

        // Derive activePluginsUi from stagedPlugins + running status
        viewModelScope.launch {
            combine(
                pluginManager.activeBotPlugins,
                _state.map { it.selectedBot }.distinctUntilChanged(),
                _state.map { it.stagedPlugins }.distinctUntilChanged()
            ) { activeBotPlugins, selectedBot, stagedPlugins ->
                val runningEntry = selectedBot?.let { activeBotPlugins[it] }
                stagedPlugins.map { plugin ->
                    PluginsUi(
                        name = plugin.name,
                        active = runningEntry?.pluginName == plugin.name && runningEntry.job.isActive,
                        pluginInfo = plugin,
                        details = plugin.description,
                        id = plugin.id
                    )
                }
            }.collect { activePluginsUi ->
                _state.update { it.copy(activePluginsUi = activePluginsUi) }
            }
        }

        handleIntent(MainBotScreenIntent.LoadPlugins)
    }

    private fun startBotCollectors(bot: L2Bot) {
        botCollectorsJob?.cancel()
        botCollectorsJob = viewModelScope.launch {
            supervisorScope {
                launch(Dispatchers.IO) {
                    bot.errors.collectLatest {
                        logController.logError(it.message ?: "Unknown")
                    }
                }

                launch(Dispatchers.IO) {
                    bot.actionEvents.collectLatest {
                        logController.logAction("${it.action.name} ${it.p1} ${it.p2}")
                    }
                }

                launch {
                    bot.packetEvents.collectLatest {
                        logController.logServerClient("${it.header} ${it.data}")
                    }
                }

                launch {
                    bot.cliPacketEvents.collectLatest {
                        logController.logClientServer("${it.header} ${it.data}")
                    }
                }

                launch {
                    while (isActive) {
                        val user = bot.user()
                        _state.update {
                            it.copy(user = user)
                        }
                        delay(5000L)
                    }
                }
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
            is MainBotScreenIntent.LoadPlugins -> loadPlugins()
            is MainBotScreenIntent.SearchPlugin -> _state.update { it.copy(searchPlugin = intent.query) }
            is MainBotScreenIntent.AddPluginToActive -> addPluginToActive(intent.plugin)
            is MainBotScreenIntent.RunPluginFromLibrary -> runPluginFromLibrary(intent.plugin)
            is MainBotScreenIntent.TogglePluginStatus -> togglePluginStatus(intent.plugin)
            is MainBotScreenIntent.RemoveActivePlugin -> removeActivePlugin(intent.plugin)
            is MainBotScreenIntent.StopAllPlugins -> stopAllPlugins()
        }
    }

    private fun loadBots() {
        viewModelScope.launch {
            _state.update {
                it.copy(bots = l2Adrenaline.getAvailableBots())
            }
        }
    }

    private fun selectBot(bot: L2Bot) {
        _state.update { it.copy(selectedBot = bot) }
        if (bot.connectionStatus.value == ConnectionStatus.CONNECTED) {
            startBotCollectors(bot)
        }
    }

    private fun toggleConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            state.value.selectedBot?.let { bot ->
                if (bot.connectionStatus.value == ConnectionStatus.DISCONNECTED) {
                    bot.connect()
                    startBotCollectors(bot)
                } else {
                    bot.disconnect()
                    botCollectorsJob?.cancel()
                    botCollectorsJob = null
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

    // Active list: toggle run/stop
    private fun togglePluginStatus(pluginUi: PluginsUi) {
        state.value.selectedBot?.let { bot ->
            if (pluginUi.active) {
                pluginManager.stopPlugin(bot)
            } else {
                pluginManager.runPlugin(pluginUi.pluginInfo, bot, viewModelScope)
            }
        }
    }

    // Active list: remove from list (stop if running)
    private fun removeActivePlugin(pluginUi: PluginsUi) {
        state.value.selectedBot?.let { bot ->
            if (pluginUi.active) {
                pluginManager.stopPlugin(bot)
            }
        }
        _state.update { state ->
            state.copy(stagedPlugins = state.stagedPlugins.filter { it.id != pluginUi.pluginInfo.id })
        }
    }

    // Active list: stop all running plugins
    private fun stopAllPlugins() {
        state.value.selectedBot?.let { bot ->
            pluginManager.stopPlugin(bot)
        }
    }
}
