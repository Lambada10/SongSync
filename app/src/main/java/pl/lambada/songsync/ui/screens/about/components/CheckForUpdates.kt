package pl.lambada.songsync.ui.screens.about.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.Release
import pl.lambada.songsync.ui.screens.about.AboutViewModel
import pl.lambada.songsync.util.showToast

@Composable
fun CheckForUpdates(
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit,
    context: Context,
    viewModel: AboutViewModel,
    version: String
) {
    var updateState by rememberSaveable { mutableStateOf(UpdateState.CHECKING) }
    var latest: Release? by rememberSaveable { mutableStateOf(null) }
    var isUpdate by rememberSaveable { mutableStateOf(false) }

    when (updateState) {
        UpdateState.CHECKING -> {
            showToast(
                context,
                stringResource(R.string.checking_for_updates),
                long = false
            )

            LaunchedEffect(Unit) {
                launch(Dispatchers.IO) {
                    try {
                        latest = viewModel.getLatestRelease()
                        isUpdate = viewModel.isNewerRelease(context)
                    } catch (e: Exception) {
                        updateState = UpdateState.ERROR
                        return@launch
                    }
                    updateState = if (isUpdate) {
                        UpdateState.UPDATE_AVAILABLE
                    } else {
                        UpdateState.UP_TO_DATE
                    }
                }
            }
        }

        UpdateState.UPDATE_AVAILABLE -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.update_available)) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text("v$version -> ${latest?.tagName}")
                        Text(stringResource(R.string.changelog, latest?.changelog!!))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDownload(latest?.htmlURL!!)
                        }
                    ) {
                        Text(stringResource(R.string.download))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        UpdateState.UP_TO_DATE -> {
            showToast(
                context,
                stringResource(R.string.up_to_date),
                long = false
            )
            onDismiss()
        }

        UpdateState.ERROR -> {
            showToast(
                context,
                stringResource(R.string.error_checking_for_updates),
                long = false
            )
            onDismiss()
        }
    }
}