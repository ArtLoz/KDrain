package org.shadow.project

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.koinInject
import org.shadow.project.di.initKoin
import org.shadow.project.ui.main.KDrainMain

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
            KDrainMain()
        }
    }
}
