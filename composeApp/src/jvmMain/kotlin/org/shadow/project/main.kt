package org.shadow.project

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.l2bot.bridge.api.L2Adrenaline
import com.l2bot.bridge.api.TransportProvider
import com.l2bot.bridge.transport.jvm.JvmTransportProvider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shadow.project.di.initKoin
import org.shadow.project.ui.App

fun main() {
    initKoin()
    L2Adrenaline.setTransportProvider(JvmTransportProvider())
    application {
        val windowController = koinInject<WindowController>()
        val mainWindowState = windowController.mainWindowState.collectAsState().value
        Window(
            state = mainWindowState,
            resizable = false,
            onCloseRequest = ::exitApplication,
            title = "KDrain",
        ) {
            App()
        }
    }
}