package pl.lambada.songsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.CombinedModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchItem(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    innerPaddingValues: PaddingValues = PaddingValues(
        horizontal = 22.dp,
        vertical = 16.dp
    ),
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
        Text(
            text = label,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = selected,
            onCheckedChange = { onClick() }
        )
    }
}
