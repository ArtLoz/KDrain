package org.shadow.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.l2bot.bridge.api.L2Adrenaline
import com.l2bot.bridge.api.TransportProvider
import com.l2bot.bridge.models.interfaces.IL2Spawn
import com.l2bot.bridge.models.interfaces.inRange
import com.l2bot.bridge.transport.jvm.JvmTransportProvider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shadow.project.di.initKoin
import org.shadow.project.ui.App
import org.shadow.project.ui.main.component.ActiveScriptsPanel
import org.shadow.project.ui.main.component.CharacterStatusBar
import org.shadow.project.ui.main.component.LibraryPanel
import org.shadow.project.ui.main.component.SubBotsPanel
import org.shadow.project.ui.theme.KDrainTheme
import kotlin.math.abs
import kotlin.math.sqrt

fun main() {
    initKoin()
    application {
        val windowController = koinInject<WindowController>()
        val mainWindowState = windowController.mainWindowState.collectAsState().value
        Window(
            state = mainWindowState,
            resizable = true,
            onCloseRequest = ::exitApplication,
            title = "KDrain",
        ) {
            KDrainTheme {
                Scaffold {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CharacterStatusBar()
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .weight(1f)
                        ) {
                            SubBotsPanel(
                                modifier = Modifier.weight(1f),
                                selectedBot = null,
                                activeBot = emptyList(), onClick = {})
                            Column(modifier = Modifier.weight(1.8f)) {
                                ActiveScriptsPanel(
                                    activePlugins = emptyList(),
                                    onChangeStatusPlugin = {},
                                    onDeletePlugin = {},
                                    stopAllPlugins = {},
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                            LibraryPanel(
                                modifier = Modifier.weight(1.2f),
                                searchPlugin = "",
                                onSearchPluginChange = {},
                                plugins = emptyList(),
                                onRunPlugin = {},
                                onAddPluginToActive = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

