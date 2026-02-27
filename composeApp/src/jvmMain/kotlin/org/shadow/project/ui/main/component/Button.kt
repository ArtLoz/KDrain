package org.shadow.project.ui.main.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ActionIconButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(30.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        onClick = onClick
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.padding(6.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}