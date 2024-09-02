package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R

@Composable
fun NoConnectionDialogue(
    onDismissRequest: () -> Unit,
    onOkRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { Button(onOkRequest) { Text(stringResource(R.string.ok)) } },
        title = { Text(text = stringResource(id = R.string.error)) },
        text = { Text(stringResource(R.string.no_internet_server)) }
    )
}