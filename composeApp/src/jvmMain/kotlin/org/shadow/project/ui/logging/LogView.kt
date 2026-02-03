package org.shadow.project.ui.logging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shadow.project.logging.LogController
import org.shadow.project.logging.LogEntry
import org.shadow.project.logging.LogType

@Composable
fun LogView(controller: LogController, modifier: Modifier = Modifier) {
    val logs by controller.logs.collectAsState()
    val enabledTypes by controller.enabledTypes.collectAsState()
    val filteredLogs = remember(logs, enabledTypes) {
        logs.filter { it.type in enabledTypes }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                LogType.entries.forEach { type ->
                    FilterChip(
                        selected = type in enabledTypes,
                        onClick = { controller.toggleType(type) },
                        label = { Text(type.displayName, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = type.color.copy(alpha = 0.2f),
                            selectedLabelColor = type.color,
                            labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(4.dp),
            tonalElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredLogs) { entry ->
                        LogItem(entry)
                    }
                }
                IconButton(
                    onClick = { controller.clear() },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear logs",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
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
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = entry.type.displayName,
            color = entry.type.color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(55.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = entry.message,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
