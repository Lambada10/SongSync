package pl.lambada.songsync.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.data.ext.BackPressHandler
import pl.lambada.songsync.ui.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentRoute: String?, selected: SnapshotStateList<String>,
    allSongs: List<Song>?
) {
    val screens = Screens.values()
    val currentScreen = screens.firstOrNull { it.name == currentRoute }
    var cachedSize by remember { mutableIntStateOf(1) }
    if (selected.size > 0) {
        // keep displaying "1 selected" during fade-out, don't say "0 selected"
        cachedSize = selected.size
    }

    BackPressHandler(enabled = selected.size > 0, onBackPressed = { selected.clear() })
    Crossfade(
        targetState = selected.size > 0,
        label = ""
    ) { showing ->
        TopAppBar(
            navigationIcon = {
                if (showing) {
                    IconButton(onClick = { selected.clear() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                id = R.string.back
                            )
                        )
                    }
                }
            },
            title = {
                if (showing) {
                    Crossfade(
                        targetState = cachedSize,
                        label = ""
                    ) { size ->
                        Text(text = stringResource(id = R.string.selected_count, size))
                    }
                } else {
                    Text(
                        text =
                        currentScreen?.let { stringResource(it.stringResource) }
                            ?: currentRoute.toString()
                    )
                }
            },
            actions = {
                if (showing) {
                    IconButton(onClick = {
                        selected.clear()
                        allSongs?.map { it.filePath }?.forEach {
                            if (it != null) {
                                selected.add(it)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.SelectAll,
                            contentDescription = stringResource(
                                id = R.string.select_all
                            )
                        )
                    }
                    IconButton(onClick = {
                        val willBeSelected = allSongs?.map { it.filePath }?.toMutableList()
                        for (song in selected) {
                            willBeSelected?.remove(song)
                        }
                        selected.clear()
                        if (willBeSelected != null) {
                            for (song in willBeSelected) {
                                song?.let { selected.add(it) }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Deselect,
                            contentDescription = stringResource(
                                id = R.string.invert_selection
                            )
                        )
                    }
                }
            },
            colors =
            if (showing) {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            } else {
                TopAppBarDefaults.topAppBarColors()
            }
        )
    }
}

@Composable
fun BottomBar(currentRoute: String?, navController: NavController) {
    NavigationBar {
        val screens = Screens.values()
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.name,
                onClick = {
                    if (currentRoute != screen.name) {
                        if (screen == Screens.Home) {
                            navController.popBackStack(
                                navController.graph.startDestinationId,
                                false
                            )
                        } else {
                            navController.navigate(screen.name)
                        }
                    }
                },
                icon = {
                    val isSelected = currentRoute == screen.name
                    val imageVector = when (screen) {
                        Screens.Home -> if (isSelected) Icons.Default.Home else Icons.Outlined.Home
                        Screens.Browse -> if (isSelected) Icons.Default.Search else Icons.Outlined.Search
                        Screens.About -> if (isSelected) Icons.Default.Info else Icons.Outlined.Info
                    }
                    Icon(imageVector, contentDescription = stringResource(screen.stringResource))
                },
                label = { Text(text = stringResource(screen.stringResource)) })
        }
    }
}