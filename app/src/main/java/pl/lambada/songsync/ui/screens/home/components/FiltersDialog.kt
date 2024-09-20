package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SwitchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersDialog(
    hideLyrics: Boolean,
    folders: List<String>,
    blacklistedFolders: List<String>,
    onDismiss: () -> Unit,
    onFilterChange: () -> Unit,
    onHideLyricsChange: (Boolean) -> Unit,
    onToggleFolderBlacklist: (String, Boolean) -> Unit
) {
    var showFolders by rememberSaveable { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column {
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp, bottom = 16.dp)
                )
                SwitchItem(
                    label = stringResource(R.string.no_lyrics_only),
                    selected = hideLyrics,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20f)),
                    innerPaddingValues = PaddingValues(
                        top = 8.dp,
                        start = 8.dp,
                        end = 10.dp,
                        bottom = 8.dp
                    )
                ) {
                    onHideLyricsChange(!hideLyrics)
                    onFilterChange()
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 26.dp)
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { showFolders = true }
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.ignore_folders))
                    }
                    IconButton(onClick = { showFolders = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                        )
                    }
                }
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }

    if (showFolders) {
        BasicAlertDialog(onDismissRequest = { showFolders = false }) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.ignore_folders),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 16.dp)
                    )
                    HorizontalDivider()
                    LazyColumn {
                        items(folders.size) { index ->
                            val folder = folders[index]
                            var checked by remember {
                                mutableStateOf(blacklistedFolders.contains(folder))
                            }

                            Row(
                                modifier = Modifier
                                    .clickable {
                                        checked = !checked
                                        onToggleFolderBlacklist(folder, checked)
                                        onFilterChange()
                                    }
                                    .padding(start = 22.dp, end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(imageVector = Icons.Outlined.Folder, contentDescription = "Folder icon")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = folder.removePrefix("/storage/emulated/0/"),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 16.dp)
                                )
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { check ->
                                        checked = check
                                        onToggleFolderBlacklist(folder, check)
                                        onFilterChange()
                                    }
                                )
                            }
                        }
                        item {
                            HorizontalDivider()
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = { showFolders = false }) {
                                    Text(stringResource(R.string.close))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
