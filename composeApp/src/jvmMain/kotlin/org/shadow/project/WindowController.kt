package org.shadow.project

import androidx.compose.ui.window.WindowState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WindowController : ViewModel() {

    private val _mainWindowState = MutableStateFlow(WindowState())
    val mainWindowState = _mainWindowState.asStateFlow()
}