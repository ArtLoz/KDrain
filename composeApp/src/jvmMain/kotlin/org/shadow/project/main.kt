package org.shadow.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.l2bot.bridge.models.events.ConnectionStatus
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shadow.project.di.initKoin
import org.shadow.project.ui.main.component.ActiveScriptsPanel
import org.shadow.project.ui.main.component.CharacterStatusBar
import org.shadow.project.ui.main.component.LibraryPanel
import org.shadow.project.ui.main.component.SubBotsPanel
import org.shadow.project.ui.main.old.MainViewModel
import org.shadow.project.ui.main.old.model.MainBotScreenIntent
import org.shadow.project.ui.main.model.PluginsUi
import org.shadow.project.ui.theme.KDrainTheme

fun main() {
    initKoin()
    application {
        val windowController = koinInject<WindowController>()
        val mainWindowState = windowController.mainWindowState.collectAsState().value
        val appIcon = useResource("win_kdarin.ico") { BitmapPainter(loadImageBitmap(it)) }
        Window(
            state = mainWindowState,
            resizable = true,
            onCloseRequest = ::exitApplication,
            title = "KDrain",
            icon = appIcon,
        ) {
            val viewModel = koinViewModel<MainViewModel>()
            val state by viewModel.state.collectAsState()
            val connectionStatus = state.selectedBot?.connectionStatus?.collectAsState()?.value

            val libraryPlugins = state.plugins
                .filter { plugin ->
                    state.searchPlugin.isBlank() ||
                            plugin.name.contains(state.searchPlugin, ignoreCase = true)
                }
                .map { plugin ->
                    PluginsUi(
                        name = plugin.name,
                        active = false,
                        pluginInfo = plugin,
                        details = plugin.description,
                        id = plugin.id
                    )
                }

            KDrainTheme {
                Scaffold {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CharacterStatusBar(
                            connectionStatus = connectionStatus ?: ConnectionStatus.DISCONNECTED,
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
                                onClick = { viewModel.processIntent(MainBotScreenIntent.SelectBot(it)) },
                                onClickRefresh = { viewModel.processIntent(MainBotScreenIntent.LoadBots) }
                            )
                            Column(modifier = Modifier.weight(1.8f)) {
                                ActiveScriptsPanel(
                                    activePlugins = state.activePluginsUi,
                                    onChangeStatusPlugin = { viewModel.processIntent(MainBotScreenIntent.TogglePluginStatus(it)) },
                                    onDeletePlugin = { viewModel.processIntent(MainBotScreenIntent.RemoveActivePlugin(it)) },
                                    stopAllPlugins = { viewModel.processIntent(MainBotScreenIntent.StopAllPlugins) },
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                            LibraryPanel(
                                modifier = Modifier.weight(1.2f),
                                searchPlugin = state.searchPlugin,
                                onSearchPluginChange = { viewModel.processIntent(MainBotScreenIntent.SearchPlugin(it)) },
                                plugins = libraryPlugins,
                                onRunPlugin = { viewModel.processIntent(MainBotScreenIntent.RunPluginFromLibrary(it.pluginInfo)) },
                                onAddPluginToActive = { viewModel.processIntent(MainBotScreenIntent.AddPluginToActive(it.pluginInfo)) },
                                onClickRefresh = { viewModel.processIntent(MainBotScreenIntent.LoadPlugins) }
                            )
                        }
                    }
                }
            }
        }
    }
}

