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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import pl.lambada.songsync.MainViewModel
import pl.lambada.songsync.ui.ScreenAbout
import pl.lambada.songsync.ui.ScreenSearch
import pl.lambada.songsync.ui.screens.home.components.BatchDownloadLyrics
import pl.lambada.songsync.ui.screens.home.components.FilterAndSongCount
import pl.lambada.songsync.ui.screens.home.components.FiltersDialog
import pl.lambada.songsync.ui.screens.home.components.HomeAppBar
import pl.lambada.songsync.ui.screens.home.components.HomeSearchBar
import pl.lambada.songsync.ui.screens.home.components.HomeSearchThing
import pl.lambada.songsync.ui.screens.home.components.SongItem
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
    viewModel: HomeViewModel = viewModel(),
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
                onBackPressed = { viewModel.selected.clear() }
            )

            Crossfade(
                targetState = viewModel.selected.size > 0,
                label = ""
            ) { showing ->
                HomeAppBar(
                    showing = showing,
                    scrollBehavior = scrollBehavior,
                    onSelectedClearAction = viewModel.selected::clear,
                    onNavigateToAboutSectionRequest = { navController.navigate(ScreenAbout) },
                    onProviderSelectRequest = viewModel.userSettingsController::updateSelectedProviders,
                    onBatchDownloadRequest = { isBatchDownload = true },
                    selectedProvider = viewModel.userSettingsController.selectedProvider,
                    onSelectAllSongsRequest = viewModel::selectAllSongs,
                    onInvertSongSelectionRequest = viewModel::invertSongSelection,
                    embedLyrics = viewModel.userSettingsController.embedLyricsIntoFiles,
                    onEmbedLyricsChangeRequest = viewModel.userSettingsController::updateEmbedLyrics,
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
                    onBatchDownloadState = { onBatchDownload -> isBatchDownload = onBatchDownload },
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
    viewModel: HomeViewModel,
    paddingValues: PaddingValues,
    isBatchDownload: Boolean,
    onBatchDownloadState: (isBatchDownload: Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.filterSongs() }

    Column {
        if (isBatchDownload) {
            BatchDownloadLyrics(
                viewModel = viewModel,
                onDone = { onBatchDownloadState(false) })
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
                        showingSearch = viewModel.showingSearch,
                        searchBar = {
                            HomeSearchBar(
                                query = viewModel.searchQuery,
                                onQueryChange = { newQuery ->
                                    viewModel.searchQuery = newQuery
                                    viewModel.updateSearchResults(newQuery.lowercaseWithLocale())
                                    viewModel.showingSearch = true
                                },
                                showSearch = viewModel.showSearch,
                                onShowSearchChange = { viewModel.showSearch = it },
                                showingSearch = viewModel.showingSearch,
                                onShowingSearchChange = { viewModel.showingSearch = it }
                            )
                        },
                        filterBar = {
                            FilterAndSongCount(
                                displaySongsCount = viewModel.displaySongs.size,
                                onFilterClick = { viewModel.showFilters = true },
                                onSearchClick = {
                                    viewModel.showSearch = true
                                    viewModel.showingSearch = true
                                }
                            )
                        }
                    )

                    if (viewModel.showFilters) {
                        FiltersDialog(
                            hideLyrics = viewModel.userSettingsController.hideLyrics,
                            folders = viewModel.getSongFolders(context),
                            blacklistedFolders = viewModel.userSettingsController.blacklistedFolders,
                            onDismiss = { viewModel.showFilters = false },
                            onFilterChange = { viewModel.filterSongs() },
                            onHideLyricsChange = viewModel::onHideLyricsChange,
                            onToggleFolderBlacklist = viewModel::onToggleFolderBlacklist
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
                            viewModel.showSearch = false
                            viewModel.showingSearch = false
                        } else {
                            selected.remove(song.filePath)
                            if (selected.size == 0 && viewModel.searchQuery.isNotEmpty())
                                viewModel.showingSearch = true // show again but don't focus
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