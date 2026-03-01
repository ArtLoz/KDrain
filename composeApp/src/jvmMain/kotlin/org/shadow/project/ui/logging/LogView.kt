package org.shadow.project.ui.logging

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shadow.project.logging.LogController
import org.shadow.project.logging.LogEntry
import org.shadow.project.logging.LogType

@Composable
fun LogView(controller: LogController, botName: String?, modifier: Modifier = Modifier) {
    val logsFlow = remember(botName) {
        botName?.let { controller.logsFor(it) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val logs by logsFlow.collectAsState(emptyList())
    val enabledTypes by controller.enabledTypes.collectAsState()
    val saveEnabled by controller.saveEnabled.collectAsState()
    val filteredLogs = remember(logs, enabledTypes) {
        logs.filter { it.type in enabledTypes }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val wasAtBottom = lastVisible >= filteredLogs.size - 2
            if (wasAtBottom) {
                listState.animateScrollToItem(filteredLogs.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogType.entries.forEach { type ->
                val selected = type in enabledTypes
                val bg = if (selected) type.color.copy(alpha = 0.2f)
                         else MaterialTheme.colorScheme.surface
                val textColor = if (selected) type.color
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(bg)
                        .clickable { controller.toggleType(type) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(type.displayName, fontSize = 9.sp, color = textColor, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save logs to file",
                tint = if (saveEnabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { controller.toggleSave() }
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { botName?.let { controller.clear(it) } }
            )
        }

        Spacer(Modifier.height(2.dp))

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(4.dp),
            tonalElevation = 2.dp
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(filteredLogs, key = { it.id }) { entry ->
                    LogItem(entry)
                }
            }
        }
    }
}

@Composable
fun LogItem(entry: LogEntry) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "[${entry.formattedTime}]",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = entry.type.displayName,
            color = entry.type.color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(50.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = entry.message,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
