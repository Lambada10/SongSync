package pl.lambada.songsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.CombinedModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SwitchItem(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    innerPaddingValues: PaddingValues = PaddingValues(
        horizontal = 22.dp,
        vertical = 16.dp
    ),
    description: String = "",
    onClick: () -> Unit,
) {
    Row(
        modifier = CombinedModifier(
            inner = Modifier
                .clickable { onClick() }
                .padding(innerPaddingValues),
            outer = modifier
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
            )
            if (description.isNotEmpty() == true) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = selected,
            onCheckedChange = { onClick() }
        )
    }
}
