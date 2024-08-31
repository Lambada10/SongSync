package pl.lambada.songsync.ui.screens.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.ui.ScreenAbout
import pl.lambada.songsync.ui.ScreenSearch
import pl.lambada.songsync.ui.screens.home.components.BatchDownloadLyrics
import pl.lambada.songsync.ui.screens.home.components.FilterAndSongCount
import pl.lambada.songsync.ui.screens.home.components.FiltersDialog
import pl.lambada.songsync.ui.screens.home.components.HomeAppBar
import pl.lambada.songsync.ui.screens.home.components.HomeSearchBar
import pl.lambada.songsync.ui.screens.home.components.HomeSearchThing
import pl.lambada.songsync.ui.screens.home.components.SongItem
import pl.lambada.songsync.util.dataStore
import pl.lambada.songsync.util.ext.BackPressHandler
import pl.lambada.songsync.util.ext.lowercaseWithLocale

/**
 * Composable function representing the home screen.
 *
 * @param viewModel The [MainViewModel] instance.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: MainViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var isBatchDownload by remember { mutableStateOf(false) }
    val context = LocalContext.current

    SideEffect { viewModel.updateAllSongs(context) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            var cachedSize by remember { mutableIntStateOf(1) }
            if (viewModel.selected.size > 0) {
                // keep displaying "1 selected" during fade-out, don't say "0 selected"
                cachedSize = viewModel.selected.size
            }

            BackPressHandler(
                enabled = viewModel.selected.size > 0,
                onBackPressed = { viewModel.selected.clear() })
            Crossfade(
                targetState = viewModel.selected.size > 0,
                label = ""
            ) { showing ->
                HomeAppBar(
                    showing = showing,
                    scrollBehavior = scrollBehavior,
                    onSelectedClearAction = viewModel.selected::clear,
                    onNavigateToAboutSectionRequest = { navController.navigate(ScreenAbout) },
                    onProviderSelectRequest = { viewModel.selectedProvider = it },
                    onBatchDownloadRequest = { isBatchDownload = true },
                    selectedProvider = viewModel.selectedProvider,
                    onSelectAllSongsRequest = viewModel::selectAllSongs,
                    onInvertSongSelectionRequest = viewModel::invertSongSelection,
                    cachedSize = cachedSize
                )
            }
        },
        floatingActionButton = {
            with(sharedTransitionScope) {
                FloatingActionButton(
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "fab"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    onClick = { navController.navigate(ScreenSearch()) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search lyrics"
                    )
                }
            }
        }
    ) { paddingValues ->
        Crossfade(viewModel.allSongs == null, label = "") { loading ->
            if (loading)
                LoadingScreen()
            else
                HomeScreenLoaded(
                    navController = navController,
                    viewModel = viewModel,
                    selected = viewModel.selected,
                    paddingValues = paddingValues,
                    isBatchDownload = isBatchDownload,
                    onBatchDownload = { onBatchDownload -> isBatchDownload = onBatchDownload },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
        }
    }
}

/**
 * Composable function representing the loading screen.
 */
@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreenLoaded(
    selected: SnapshotStateList<String>,
    navController: NavHostController,
    viewModel: MainViewModel,
    paddingValues: PaddingValues,
    isBatchDownload: Boolean,
    onBatchDownload: (Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    var showingSearch by rememberSaveable { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(showingSearch) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val dataStore = context.dataStore

    LaunchedEffect(Unit) {
        viewModel.filterSongs()
    }

    Column {
        if (isBatchDownload) {
            BatchDownloadLyrics(
                viewModel = viewModel,
                onDone = { onBatchDownload(false) })
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(
                    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp, start = 22.dp, end = 4.dp),
                ) {
                    HomeSearchThing(
                        showingSearch = showingSearch,
                        searchBar = {
                            HomeSearchBar(
                                query = viewModel.searchQuery,
                                onQueryChange = { newQuery ->
                                    viewModel.searchQuery = newQuery
                                    viewModel.updateSearchResults(newQuery.lowercaseWithLocale())
                                    showingSearch = true
                                },
                                showSearch = showSearch,
                                onShowSearchChange = { showSearch = it },
                                showingSearch = showingSearch,
                                onShowingSearchChange = { showingSearch = it }
                            )
                        },
                        filterBar = {
                            FilterAndSongCount(
                                displaySongsCount = viewModel.displaySongs.size,
                                onFilterClick = { showFilters = true },
                                onSearchClick = {
                                    showSearch = true
                                    showingSearch = true
                                }
                            )
                        }
                    )

                    if (showFilters) {
                        FiltersDialog(
                            hideLyrics = viewModel.hideLyrics,
                            folders = viewModel.getSongFolders(context),
                            blacklistedFolders = viewModel.blacklistedFolders,
                            onDismiss = { showFilters = false },
                            onFilterChange = { viewModel.filterSongs() },
                            onHideLyricsChange = { viewModel.onHideLyricsChange(dataStore, it) },
                            onToggleFolderBlacklist = { folder, blacklisted ->
                                viewModel.onToggleFolderBlacklist(dataStore, folder, blacklisted)
                            }
                        )
                    }
                }
            }

            items(viewModel.displaySongs.size) { index ->
                val song = viewModel.displaySongs[index]

                SongItem(
                    id = index.toString(),
                    selected = selected.contains(song.filePath),
                    quickSelect = selected.size > 0,
                    onSelectionChanged = { newValue ->
                        if (newValue) {
                            song.filePath?.let { selected.add(it) }
                            showSearch = false
                            showingSearch = false
                        } else {
                            selected.remove(song.filePath)
                            if (selected.size == 0 && viewModel.searchQuery.isNotEmpty())
                                showingSearch = true // show again but don't focus
                        }
                    },
                    navController = navController,
                    song = song,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    viewModel = viewModel,
                )
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}