package pl.lambada.songsync.ui.screens.init.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionItem(
    @StringRes title: Int,
    @StringRes description: Int,
    onClick: () -> Unit,
    granted: Boolean,
    innerPaddingValues: PaddingValues = PaddingValues(
        horizontal = 8.dp,
        vertical = 16.dp
    ),
) {
    Row(
        modifier = Modifier
            .clickable(!granted) { onClick() }
            .padding(innerPaddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(id = title),
            )
            Text(
                text = stringResource(id = description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        if (granted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Launch,
                contentDescription = null,
            )
        }
    }
}