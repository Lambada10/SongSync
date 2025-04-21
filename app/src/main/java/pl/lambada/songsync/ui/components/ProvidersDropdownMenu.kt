package pl.lambada.songsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.util.Providers
import pl.lambada.songsync.util.dataStore
import pl.lambada.songsync.util.set

@Composable
fun ProvidersDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedProvider: Providers,
    onProviderSelectRequest: (Providers) -> Unit,
) {
    val providers = Providers.entries.toTypedArray()
    val dataStore = LocalContext.current.dataStore
    val scope = rememberCoroutineScope()
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(id = R.string.provider),
            modifier = Modifier.padding(start = 18.dp, top = 8.dp),
            fontSize = 12.sp
        )
        providers.forEach {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = it.displayName,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                        if (it.hasWordByWord)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                                    .size(14.dp)
                            ) {
                                Text(
                                    text = "W",
                                    color = MaterialTheme.colorScheme.background,
                                    style = TextStyle(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                )
                            }
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = selectedProvider == it,
                            onClick = {
                                onProviderSelectRequest(it)
                                dataStore.set(
                                    stringPreferencesKey("provider"),
                                    it.displayName
                                )
                                scope.launch {
                                    delay(200)
                                    onDismissRequest()
                                }
                            }
                        )
                    }
                },
                onClick = {
                    onProviderSelectRequest(it)
                    dataStore.set(
                        stringPreferencesKey("provider"),
                        it.displayName
                    )
                    scope.launch {
                        delay(200)
                        onDismissRequest()
                    }
                }
            )
        }
    }
}
