package org.shadow.project.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.l2bot.bridge.models.events.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.shadow.project.logging.LogController
import org.shadow.project.ui.logging.LogView
import org.shadow.project.ui.main.component.ActiveScriptsPanel
import org.shadow.project.ui.main.component.CharacterStatusBar
import org.shadow.project.ui.main.component.ConfigDialog
import org.shadow.project.ui.main.component.LibraryPanel
import org.shadow.project.ui.main.component.SubBotsPanel
import org.shadow.project.ui.main.model.MainBotScreenIntent
import org.shadow.project.ui.theme.KDrainTheme

@Composable
fun KDrainMain(modifier: Modifier = Modifier){
    val viewModel = koinViewModel<MainViewModel>()
    val state by viewModel.state.collectAsState()
    val logController = koinInject<LogController>()
    val disconnectedFlow = remember { MutableStateFlow(ConnectionStatus.DISCONNECTED) }
    val connectionStatus = (state.selectedBot?.connectionStatus ?: disconnectedFlow).collectAsState().value

    KDrainTheme {
        Scaffold {
            Column(modifier = Modifier.fillMaxSize()) {
                CharacterStatusBar(
                    connectionStatus = connectionStatus,
                    user = state.user,
                    onClickAction = { viewModel.processIntent(MainBotScreenIntent.ToggleConnection) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                ) {
                    SubBotsPanel(
                        modifier = Modifier.weight(1f),
                        selectedBot = state.selectedBot,
                        activeBot = state.bots,
                        botColorMap = state.botColorMap,
                        onClick = { viewModel.processIntent(MainBotScreenIntent.SelectBot(it)) },
                        onToggleConnection = { viewModel.processIntent(MainBotScreenIntent.ToggleBotConnection(it)) },
                        onClickRefresh = { viewModel.processIntent(MainBotScreenIntent.LoadBots) }
                    )
                    Column(modifier = Modifier.weight(1.8f)) {
                        ActiveScriptsPanel(
                            activePlugins = state.activePluginsUi,
                            selectedBotName = state.selectedBot?.charName,
                            onChangeStatusPlugin = { viewModel.processIntent(MainBotScreenIntent.TogglePluginOnSelectedBot(it)) },
                            onDeletePlugin = { viewModel.processIntent(MainBotScreenIntent.RemoveActivePlugin(it)) },
                            onStopPluginOnBot = { pluginId, configId, botName ->
                                viewModel.processIntent(MainBotScreenIntent.StopPluginOnBot(pluginId, configId, botName))
                            },
                            stopAllPlugins = { viewModel.processIntent(MainBotScreenIntent.StopAllPlugins) },
                            onEditConfig = { viewModel.processIntent(MainBotScreenIntent.EditConfig(it)) },
                            modifier = Modifier.weight(2f).padding(horizontal = 8.dp)
                        )
                        LogView(
                            controller = logController,
                            botName = state.selectedBot?.charName,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                    }
                    LibraryPanel(
                        modifier = Modifier.weight(1.2f),
                        searchPlugin = state.searchPlugin,
                        onSearchPluginChange = { viewModel.processIntent(MainBotScreenIntent.SearchPlugin(it)) },
                        plugins = state.libraryPluginsUi,
                        onRunPlugin = { viewModel.processIntent(MainBotScreenIntent.RunPluginFromLibrary(it.pluginInfo)) },
                        onAddPluginToActive = { viewModel.processIntent(MainBotScreenIntent.AddPluginToActive(it.pluginInfo)) },
                        onClickRefresh = { viewModel.processIntent(MainBotScreenIntent.LoadPlugins) }
                    )
                }
            }

            // Config dialog overlay
            state.configDialogState?.let { dialogState ->
                ConfigDialog(
                    state = dialogState,
                    onValueChange = { key, value ->
                        viewModel.processIntent(MainBotScreenIntent.UpdateConfigDialogValue(key, value))
                    },
                    onLabelChange = { viewModel.processIntent(MainBotScreenIntent.UpdateConfigDialogLabel(it)) },
                    onCopyFrom = { viewModel.processIntent(MainBotScreenIntent.CopyConfigFrom(it)) },
                    onConfirm = { viewModel.processIntent(MainBotScreenIntent.ConfirmConfigDialog) },
                    onDismiss = { viewModel.processIntent(MainBotScreenIntent.DismissConfigDialog) }
                )
            }
        }
    }
}
