package org.shadow.project.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2bot.bridge.api.L2Adrenaline
import com.l2bot.bridge.api.L2Bot
import com.l2bot.bridge.models.events.ConnectionStatus
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.shadow.project.logging.LogController
import org.shadow.project.plugin.PluginManager
import org.shadow.project.ui.main.model.MainBotScreenIntent
import org.shadow.project.ui.main.model.MainBotStateIntent

class MainViewModel(
    private val l2Adrenaline: L2Adrenaline,
    private val logController: LogController,
    private val pluginManager: PluginManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainBotStateIntent())
    val state = _state.asStateFlow()
    private var botCollectorsJob: CompletableJob? = null

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
        viewModelScope.launch {
            pluginManager.activePlugin.collect { active ->
                _state.update { it.copy(activePlugin = active) }
            }
        }

        handleIntent(MainBotScreenIntent.LoadPlugins)
    }

    private fun startBotCollectors(bot: L2Bot) {
        botCollectorsJob?.cancel()
        botCollectorsJob = null
        botCollectorsJob = SupervisorJob()

        viewModelScope.launch(botCollectorsJob!! + Dispatchers.IO) {
            bot.errors.collectLatest {
                logController.logError(it.message ?: "Unknown")
            }
        }

        viewModelScope.launch(botCollectorsJob!! + Dispatchers.IO) {
            bot.actionEvents.collectLatest {
                logController.logAction("${it.action.name} ${it.p1} ${it.p2}")
            }
        }

        viewModelScope.launch(botCollectorsJob!!) {
            bot.packetEvents.collectLatest {
                logController.logServerClient("${it.header} ${it.data}")
            }
        }

        viewModelScope.launch(botCollectorsJob!!) {
            bot.cliPacketEvents.collectLatest {
                logController.logClientServer("${it.header} ${it.data}")
            }
        }

        viewModelScope.launch(botCollectorsJob!!) {
            while (isActive) {
                _state.update {
                    it.copy(charterInfo = bot.user())
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
            is MainBotScreenIntent.RunTestScript -> runTestScript()
            is MainBotScreenIntent.LoadPlugins -> loadPlugins()
            is MainBotScreenIntent.RunPlugin -> runPlugin(intent)
            is MainBotScreenIntent.StopPlugin -> stopPlugin()
        }
    }

    private fun loadBots() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    bots = l2Adrenaline.getAvailableBots()
                )
            }
        }
    }

    private fun selectBot(bot: L2Bot) {
        viewModelScope.launch {
            _state.update { it.copy(selectedBot = bot) }
        }
        if(bot.connectionStatus.value == ConnectionStatus.CONNECTED){
            startBotCollectors(bot)
        }
    }

    private fun toggleConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            state.value.selectedBot?.let { bot ->
                if(bot.connectionStatus.value == ConnectionStatus.DISCONNECTED){
                    bot.connect()
                    startBotCollectors(bot)
                }else {
                    bot.disconnect()
                    botCollectorsJob?.cancel()
                    botCollectorsJob = null

                }
            }
        }
    }

    private fun runTestScript() {
        state.value.selectedBot?.let { bot ->
            bot.launch {
                goldScript(bot)
            }
        }
    }

    private fun loadPlugins() {
        viewModelScope.launch(Dispatchers.IO) {
            pluginManager.loadPlugins()
        }
    }

    private fun runPlugin(intent: MainBotScreenIntent.RunPlugin) {
        state.value.selectedBot?.let { bot ->
            pluginManager.runPlugin(intent.plugin, bot, viewModelScope)
        }
    }

    private fun stopPlugin() {
        pluginManager.stopActivePlugin()
    }
}
