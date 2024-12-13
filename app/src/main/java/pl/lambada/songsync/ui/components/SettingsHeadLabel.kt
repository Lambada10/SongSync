package pl.lambada.songsync.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsHeadLabel(
    label: String,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(
            start = 22.dp,
            top = 22.dp,
            end = 22.dp,
            bottom = 0.dp
        ),
        color = MaterialTheme.colorScheme.primary
    )
}