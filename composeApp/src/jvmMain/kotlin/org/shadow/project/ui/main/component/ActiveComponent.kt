package org.shadow.project.ui.main.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shadow.project.ui.main.model.BotRunInfo
import org.shadow.project.ui.main.model.PluginsUi


@Composable
fun ActiveScriptsPanel(
    activePlugins: List<PluginsUi>,
    selectedBotName: String?,
    onChangeStatusPlugin: (PluginsUi) -> Unit,
    onDeletePlugin: (PluginsUi) -> Unit,
    onStopPluginOnBot: (pluginId: String, botCharName: String) -> Unit,
    stopAllPlugins: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxHeight()
    ) {
        Column() {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE PLUGINS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // TextSecondary
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                    onClick = stopAllPlugins
                ) {
                    Text(
                        text = "Pause All",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface, // TextPrimary
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activePlugins, key = { it.id }) { plugin ->
                    ScriptCard(
                        name = plugin.name,
                        statusDetail = plugin.details,
                        version = plugin.pluginInfo.version,
                        author = plugin.pluginInfo.author,
                        icon = Icons.Default.AdsClick,
                        isSelectedBotRunning = plugin.active,
                        runningBots = plugin.runningBots,
                        onClickChangeStatus = { onChangeStatusPlugin(plugin) },
                        onClickDelete = { onDeletePlugin(plugin) },
                        onStopBot = { botCharName -> onStopPluginOnBot(plugin.id, botCharName) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScriptCard(
    name: String,
    statusDetail: String?,
    version: String,
    author: String,
    icon: ImageVector,
    isSelectedBotRunning: Boolean,
    runningBots: List<BotRunInfo>,
    onClickChangeStatus: () -> Unit,
    onClickDelete: () -> Unit,
    onStopBot: (String) -> Unit
) {
    val hasAnyRunning = runningBots.isNotEmpty()
    val statusColor =
        if (hasAnyRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        border = if (hasAnyRunning) BorderStroke(1.dp, statusColor.copy(alpha = 0.2f)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (hasAnyRunning) statusColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        color = if (hasAnyRunning) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "v$version by $author",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        val statusText = when {
                            runningBots.size == 1 -> "RUNNING ON ${runningBots[0].botCharName}"
                            runningBots.size > 1 -> "RUNNING ON ${runningBots.size} BOTS"
                            else -> "STOPPED"
                        }
                        Text(
                            statusText,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ActionIconButton(
                        if (isSelectedBotRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        onClickChangeStatus
                    )
                    ActionIconButton(Icons.Default.Delete, onClickDelete)
                }
            }

            // Bot chips row
            if (runningBots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    runningBots.forEach { botInfo ->
                        BotChip(
                            botName = botInfo.botCharName,
                            color = botInfo.color,
                            onStop = { onStopBot(botInfo.botCharName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BotChip(
    botName: String,
    color: Color,
    onStop: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = botName,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Stop on $botName",
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onStop)
            )
        }
    }
}
