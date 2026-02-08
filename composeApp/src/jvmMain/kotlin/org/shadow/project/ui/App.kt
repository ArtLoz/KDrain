package org.shadow.project.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l2bot.bridge.models.events.ConnectionStatus
import kdrain.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shadow.project.logging.LogController
import org.shadow.project.plugin.PluginInfo
import org.shadow.project.ui.logging.LogView
import org.shadow.project.ui.main.MainViewModel
import org.shadow.project.ui.main.model.MainBotScreenIntent
import org.shadow.project.ui.theme.KDrainTheme

@Composable
@Preview
fun App() {
    val logController = koinInject<LogController>()

    KDrainTheme {
        Row(
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            Controller(modifier = Modifier.weight(1f))
            InfoBlock(
                logController = logController,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Controller(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<MainViewModel>()
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val connectionState = state.selectedBot?.connectionStatus?.collectAsState(ConnectionStatus.DISCONNECTED)?.value

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                    if (it) viewModel.processIntent(MainBotScreenIntent.LoadBots)
                },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = state.selectedBot?.charName ?: stringResource(Res.string.select_bot),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryEditable
                    ).fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.bots.forEach { bot ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(modifier = Modifier.size(6.dp)) {
                                         drawCircle(if (bot.connectionStatus.value == ConnectionStatus.CONNECTED) Color.Green else Color.Gray)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(bot.charName, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                viewModel.processIntent(MainBotScreenIntent.SelectBot(bot))
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    state.selectedBot?.let {
                        viewModel.processIntent(MainBotScreenIntent.ToggleConnection)
                    }
                },
                enabled = state.selectedBot != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (connectionState == ConnectionStatus.CONNECTED) Color(
                        0xBF4CAF50
                    ) else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (connectionState == ConnectionStatus.CONNECTED) "ON" else "OFF",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                val char = state.charterInfo
                if (char != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(char.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(Res.string.level_label, char.level),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    StatBar(char.curCp, char.maxCp, Color(0xFFFDD835))
                    Spacer(Modifier.height(2.dp))
                    StatBar(char.curHp, char.maxHp, Color(0xFFE53935))
                    Spacer(Modifier.height(2.dp))
                    StatBar(char.curMp, char.maxMp, Color(0xFF1E88E5))
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(Res.string.no_bot_selected),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        PluginSection(
            plugins = state.plugins,
            activePlugin = state.activePlugin,
            isBotConnected = connectionState == ConnectionStatus.CONNECTED,
            onRefresh = { viewModel.processIntent(MainBotScreenIntent.LoadPlugins) },
            onRun = { plugin -> viewModel.processIntent(MainBotScreenIntent.RunPlugin(plugin)) },
            onStop = { viewModel.processIntent(MainBotScreenIntent.StopPlugin) },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}

@Composable
fun PluginSection(
    plugins: List<PluginInfo>,
    activePlugin: String?,
    isBotConnected: Boolean,
    onRefresh: () -> Unit,
    onRun: (PluginInfo) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.plugins),
                style = MaterialTheme.typography.titleSmall
            )
            IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.refresh),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        if (plugins.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(Res.string.no_plugins_found),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(plugins, key = { it.name }) { plugin ->
                    val isRunning = activePlugin == plugin.name
                    PluginRow(
                        plugin = plugin,
                        isRunning = isRunning,
                        canRun = isBotConnected && activePlugin == null,
                        onRun = { onRun(plugin) },
                        onStop = onStop
                    )
                }
            }
        }
    }
}

@Composable
fun PluginRow(
    plugin: PluginInfo,
    isRunning: Boolean,
    canRun: Boolean,
    onRun: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plugin.name,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    plugin.jarFile.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isRunning) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Stop", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Button(
                    onClick = onRun,
                    enabled = canRun,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Run", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun StatBar(current: Long, max: Long, color: Color) {
    val fraction = if (max > 0) current.toFloat() / max else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .background(color.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .background(color)
        )
        Text(
            text = "$current / $max",
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InfoBlock(logController: LogController, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        LogView(controller = logController, modifier = Modifier.fillMaxSize())
    }
}
