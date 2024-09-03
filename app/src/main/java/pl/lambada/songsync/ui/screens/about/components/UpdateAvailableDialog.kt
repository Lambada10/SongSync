package pl.lambada.songsync.ui.screens.about.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R

@Composable
fun UpdateAvailableDialog(
    onDismiss: () -> Unit,
    currentVersion: String,
    latestVersion: String,
    changelog: String,
    onDownloadRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_available)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("v$currentVersion -> $latestVersion")
                Text(stringResource(R.string.changelog, changelog))
            }
        },
        confirmButton = {
            Button(onDownloadRequest) { Text(stringResource(R.string.download)) }
        },
        dismissButton = {
            OutlinedButton(onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}