package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.NoTrackFoundException
import java.io.FileNotFoundException

@Composable
fun FailedDialogue(
    onDismissRequest: () -> Unit,
    onOkRequest: () -> Unit,
    exception: Exception
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { Button(onOkRequest) { Text(stringResource(R.string.ok)) } },
        title = { Text(text = stringResource(id = R.string.error)) },
        text = {
            when (exception) {
                is NoTrackFoundException -> Text(stringResource(R.string.no_results))

                is EmptyQueryException -> Text(stringResource(R.string.invalid_query))

                is FileNotFoundException -> { // Rate limit
                    Column {
                        Text(text = stringResource(R.string.spotify_api_rate_limit_reached))
                        Text(text = stringResource(R.string.please_try_again_later))
                        Text(text = stringResource(R.string.change_api_strategy))
                    }
                }

                else -> Text(exception.toString())
            }
        }
    )
}