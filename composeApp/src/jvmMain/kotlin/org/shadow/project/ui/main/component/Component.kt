package org.shadow.project.ui.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.l2bot.bridge.models.entities.L2User
import com.l2bot.bridge.models.events.ConnectionStatus
import kdrain.composeapp.generated.resources.Res
import kdrain.composeapp.generated.resources.loc_unknow
import kdrain.composeapp.generated.resources.no_bot_selected
import org.jetbrains.compose.resources.stringResource
import org.shadow.project.ui.main.KDrainMain
import org.shadow.project.ui.theme.KDrainTheme

@Composable
fun CharacterStatusBar(
    connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    onClickAction: () -> Unit = {},
    user: L2User? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileInfo(user = user)
            Spacer(modifier = Modifier.width(24.dp))
            VerticalDivider(modifier = Modifier.height(52.dp))
            Spacer(modifier = Modifier.width(24.dp))
            StatsBars(modifier = Modifier.weight(1f), user = user)
            Spacer(modifier = Modifier.width(14.dp))
            VerticalDivider(modifier = Modifier.height(52.dp))
            Spacer(modifier = Modifier.width(12.dp))
            ActionSection(
                status = connectionStatus,
                onClickAction = onClickAction
            )
        }
    }
}

@Composable
fun ProfileInfo(user: L2User?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user?.name ?: stringResource(Res.string.no_bot_selected),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LVL ${user?.level}",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.loc_unknow),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun StatsBars(modifier: Modifier = Modifier, user: L2User?) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {

        StatProgressBarNoLabel(
            current = user?.curCp?.toInt() ?: 0,
            max = user?.maxCp?.toInt() ?: 0,
            color = MaterialTheme.colorScheme.tertiary
        )
        StatProgressBar(
            label = "HP",
            current = user?.curHp?.toInt() ?: 0,
            max = user?.maxHp?.toInt() ?: 0,
            color = MaterialTheme.colorScheme.error
        )
        StatProgressBar(
            label = "MP",
            current = user?.curMp?.toInt() ?: 0,
            max = user?.maxMp?.toInt() ?: 0,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun StatProgressBar(label: String, current: Int, max: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .clip(RoundedCornerShape(2.dp))
                .background(color.copy(alpha = 0.1f)), // Фон полоски
            contentAlignment = Alignment.Center
        ) {

            LinearProgressIndicator(
                progress = { current.toFloat() / max },
                modifier = Modifier.fillMaxSize(),
                strokeCap = StrokeCap.Butt,
                color = color,
                trackColor = Color.Transparent,
            )
            Text(
                text = "$current / $max",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatProgressBarNoLabel(current: Int, max: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(
                progress = { current.toFloat() / max },
                modifier = Modifier.fillMaxSize().padding(end = 4.dp),
                strokeCap = StrokeCap.Butt,
                color = color,
                trackColor = Color.Transparent,
            )
        }
    }
}

@Composable
fun ActionSection(status: ConnectionStatus, onClickAction: () -> Unit) {
    val color = when (status) {
        ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
        ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
        ConnectionStatus.ERROR -> MaterialTheme.colorScheme.error
    }
    val invertColor = when (status) {
        ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.primary
    }
    val icon = when (status) {
        ConnectionStatus.CONNECTED -> Icons.Default.Stop
        else -> Icons.Default.PlayArrow
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "STATUS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = status.name,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        VerticalDivider(modifier = Modifier.height(52.dp),)
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(6.dp),
            color = invertColor,
            onClick = onClickAction
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun CharacterStatusBarPreview() {
    KDrainTheme {
        Scaffold {
            CharacterStatusBar()
        }
    }

}