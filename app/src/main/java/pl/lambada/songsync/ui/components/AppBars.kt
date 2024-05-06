package pl.lambada.songsync.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.ui.Screens
import pl.lambada.songsync.util.ext.BackPressHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    viewModel: MainViewModel,
    currentRoute: String?,
    selected: SnapshotStateList<String>,
    allSongs: List<Song>?,
    scrollBehavior: TopAppBarScrollBehavior,
    navController: NavController,
) {
    val screens = Screens.values()
    val currentScreen = screens.firstOrNull { it.name == currentRoute }
    var cachedSize by remember { mutableIntStateOf(1) }
    if (selected.size > 0) {
        // keep displaying "1 selected" during fade-out, don't say "0 selected"
        cachedSize = selected.size
    }
    var ableToSelect by remember { mutableStateOf<List<Song>?>(null) }

    ableToSelect = viewModel.cachedFilteredSongs ?: allSongs

    BackPressHandler(enabled = selected.size > 0, onBackPressed = { selected.clear() })
    Crossfade(
        targetState = selected.size > 0,
        label = ""
    ) { showing ->
        MediumTopAppBar(
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
                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = stringResource(id = R.string.selected_count, size)
                        )
                    }
                } else {
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = if (currentScreen?.let { stringResource(it.stringResource) } == "Home") {
                            "SongSync"
                        } else { currentRoute.toString() }
                    )
                }
            },
            actions = {
                if (showing) {
                    IconButton(onClick = {
                        selected.clear()
                        ableToSelect?.map { it.filePath }?.forEach {
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
                        val willBeSelected = ableToSelect?.map { it.filePath }?.toMutableList()
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
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert, 
                            contentDescription = "More"
                        )
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(100f)),
                    ) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Batch sync lyrics") },
                                onClick = { /*TODO*/ }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "About") },
                                onClick = {
                                    navController.navigate("About")
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            },
            colors = if (showing) {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            } else {
                TopAppBarDefaults.topAppBarColors()
            },
            scrollBehavior = scrollBehavior,
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
                        Screens.Search -> if (isSelected) Icons.Default.Search else Icons.Outlined.Search
                        Screens.About -> if (isSelected) Icons.Default.Info else Icons.Outlined.Info
                    }
                    Icon(imageVector, contentDescription = stringResource(screen.stringResource))
                },
                label = { Text(text = stringResource(screen.stringResource)) })
        }
    }
}