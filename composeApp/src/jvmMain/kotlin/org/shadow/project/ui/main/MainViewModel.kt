package org.shadow.project.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l2bot.bridge.api.L2Adrenaline
import com.l2bot.bridge.api.L2Bot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.shadow.project.logging.LogController
import org.shadow.project.ui.main.model.MainBotScreenIntent
import org.shadow.project.ui.main.model.MainBotStateIntent

class MainViewModel(
    private val logController: LogController
) : ViewModel() {

    private val _state = MutableStateFlow(MainBotStateIntent())
    val state = _state.asStateFlow()

    private val _intents = Channel<MainBotScreenIntent>(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            _intents.receiveAsFlow().collect { intent ->
                handleIntent(intent)
            }
        }
        viewModelScope.launch {
            state.collectLatest {
                if (it.selectedBot != null) {
                    launchBotCollectors(it.selectedBot)
                }
            }
        }
        handleIntent(MainBotScreenIntent.LoadBots)
    }

    private fun CoroutineScope.launchBotCollectors(bot: L2Bot) {

        launch(Dispatchers.IO) {
            bot.errors.collectLatest {
                logController.logError(it.message ?: "Unknow")
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
    }

    fun processIntent(intent: MainBotScreenIntent) {
        viewModelScope.launch { _intents.send(intent) }
    }

    private fun handleIntent(intent: MainBotScreenIntent) {
        when (intent) {
            is MainBotScreenIntent.LoadBots -> loadBots()
            is MainBotScreenIntent.SelectBot -> selectBot(intent.bot)
            is MainBotScreenIntent.ToggleConnection -> toggleConnection()
        }
    }

    private fun loadBots() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    bots = L2Adrenaline.getAvailableBots()
                )
            }
        }
    }

    private fun selectBot(bot: L2Bot) {
        _state.update { it.copy(selectedBot = bot) }
    }

    private fun toggleConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            state.value.selectedBot?.connect()
        }
    }
}
