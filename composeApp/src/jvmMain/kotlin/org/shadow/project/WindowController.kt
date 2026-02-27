package org.shadow.project

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WindowController : ViewModel() {

    private val _mainWindowState = MutableStateFlow(WindowState(
        size = DpSize(1024.dp, 720.dp)
    ))
    val mainWindowState = _mainWindowState.asStateFlow()
}