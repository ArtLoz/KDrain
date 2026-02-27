package org.shadow.project.ui.main.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import com.l2bot.bridge.api.L2Bot
import com.l2bot.bridge.models.events.ConnectionStatus
import org.shadow.project.ui.theme.AccentBlue
import org.shadow.project.ui.theme.TextPrimary
import org.shadow.project.ui.theme.TextSecondary

@Composable
fun SubBotsPanel(
    activeBot: List<L2Bot>,
    selectedBot: L2Bot?,
    botColorMap: Map<String, Color>,
    onClick: (L2Bot) -> Unit,
    onToggleConnection: (L2Bot) -> Unit,
    onClickRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxHeight()
    ) {
        Column {

            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("BOTS", color = TextSecondary, fontWeight = FontWeight.Black, fontSize = 12.sp)


                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = AccentBlue.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable(onClick = onClickRefresh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(2.dp)
                    )
                }
                Surface(color = AccentBlue.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        "${activeBot.size} Active",
                        color = AccentBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeBot, key = { it.charName }) { bot ->
                    val connectionState = bot.connectionStatus.collectAsState().value
                    SubBotCard(
                        name = bot.charName,
                        status = connectionState,
                        isSelected = bot == selectedBot,
                        botColor = botColorMap[bot.charName],
                        onClick = { onClick(bot) },
                        onToggleConnection = { onToggleConnection(bot) }
                    )
                }
            }
        }
    }

}


@Composable
fun SubBotCard(
    name: String,
    status: ConnectionStatus,
    isSelected: Boolean,
    botColor: Color?,
    onClick: () -> Unit,
    onToggleConnection: () -> Unit
) {
    val color = when (status) {
        ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
        ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
        ConnectionStatus.ERROR -> MaterialTheme.colorScheme.error
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        border = if (isSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Color indicator strip
            if (botColor != null) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(botColor)
                )
            }
            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(status.name, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val icon = when (status) {
                            ConnectionStatus.CONNECTED -> Icons.Default.Stop
                            else -> Icons.Default.PlayArrow
                        }
                        BotSmallButton(icon = icon, onClick = onToggleConnection)
                    }
                }
            }
        }
    }
}

@Composable
fun BotSmallButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        onClick = onClick
    ) {
        Icon(icon, null, modifier = Modifier.padding(6.dp), tint = TextPrimary)
    }
}
