package pl.lambada.songsync.ui.screens.about.components

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.AboutItem


@Composable
fun SdCardPathSetting(sdPath: String?, onClearPath: () -> Unit, onUpdatePath: (String) -> Unit) {
    var picker by remember { mutableStateOf(false) }
    AboutItem(
        label = stringResource(R.string.sd_card),
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Text(stringResource(R.string.set_sd_path))
        Text(
            text = if (sdPath.isNullOrEmpty()) stringResource(R.string.no_sd_card_path_set)
            else stringResource(R.string.sd_card_path_set_successfully),
            color = if (sdPath.isNullOrEmpty()) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.tertiary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onClearPath) {
                Text(stringResource(R.string.clear_sd_card_path))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = { picker = true }) {
                Text(stringResource(R.string.set_sd_card_path))
            }
        }

        if (picker) {
            val sdCardPicker =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
                    it?.let { uri -> onUpdatePath(uri.toString()) }
                    picker = false
                }
            LaunchedEffect(Unit) {
                sdCardPicker.launch(Uri.parse(Environment.getExternalStorageDirectory().absolutePath))
            }
        }
    }
}