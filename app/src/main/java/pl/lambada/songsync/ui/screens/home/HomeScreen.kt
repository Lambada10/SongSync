package pl.lambada.songsync.ui.screens.home

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.ui.LyricsFetchScreen
import pl.lambada.songsync.ui.ScreenSettings
import pl.lambada.songsync.ui.screens.home.components.BatchDownloadLyrics
import pl.lambada.songsync.ui.screens.home.components.FilterAndSongCount
import pl.lambada.songsync.ui.screens.home.components.FiltersDialog
import pl.lambada.songsync.ui.screens.home.components.HomeAppBar
import pl.lambada.songsync.ui.screens.home.components.HomeSearchBar
import pl.lambada.songsync.ui.screens.home.components.HomeSearchThing
import pl.lambada.songsync.ui.screens.home.components.SongItem
import pl.lambada.songsync.ui.screens.home.components.SortDialog
import pl.lambada.songsync.util.ext.BackPressHandler
import pl.lambada.songsync.util.ext.lowercaseWithLocale
import pl.lambada.songsync.util.ui.SearchFABBoundsTransform

/**
 * Composable function representing the home screen.
 *
 * @param viewModel The [HomeViewModel] instance.
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

    LaunchedEffect(viewModel.userSettingsController.sortBy to viewModel.userSettingsController.sortOrder) {
        viewModel.cachedSongs = null
        viewModel.updateAllSongs(context, viewModel.userSettingsController.sortBy, viewModel.userSettingsController.sortOrder)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            var cachedSize by remember { mutableIntStateOf(1) }
            if (viewModel.selectedSongs.size > 0) {
                // keep displaying "1 selected" during fade-out, don't say "0 selected"
                cachedSize = viewModel.selectedSongs.size
            }

            BackPressHandler(
                enabled = viewModel.selectedSongs.size > 0,
                onBackPressed = { viewModel.selectedSongs.clear() }
            )

            Crossfade(
                targetState = viewModel.selectedSongs.size > 0,
                label = ""
            ) { showing ->
                HomeAppBar(
                    showing = showing,
                    scrollBehavior = scrollBehavior,
                    onSelectedClearAction = viewModel.selectedSongs::clear,
                    onNavigateToSettingsSectionRequest = { navController.navigate(ScreenSettings) },
                    onProviderSelectRequest = viewModel.userSettingsController::updateSelectedProviders,
                    onBatchDownloadRequest = { isBatchDownload = true },
                    selectedProvider = viewModel.userSettingsController.selectedProvider,
                    onSelectAllSongsRequest = viewModel::selectAllDisplayingSongs,
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
                    modifier = Modifier
                        .skipToLookaheadSize()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "fab"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = SearchFABBoundsTransform,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        ),
                    onClick = { navController.navigate(LyricsFetchScreen()) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search lyrics"
                    )
                }
            }
        },
        bottomBar = { Spacer(Modifier.navigationBarsPadding()) } // fixing broken edge to edge here
    ) { paddingValues ->
        Crossfade(viewModel.allSongs == null, label = "") { loading ->
            if (loading)
                LoadingScreen()
            else
                HomeScreenLoaded(
                    navController = navController,
                    viewModel = viewModel,
                    selected = viewModel.selectedSongs,
                    scaffoldPadding = paddingValues,
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

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenLoaded(
    selected: SnapshotStateList<String>,
    navController: NavHostController,
    viewModel: HomeViewModel,
    scaffoldPadding: PaddingValues,
    isBatchDownload: Boolean,
    onBatchDownloadState: (isBatchDownload: Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val refreshState = rememberPullToRefreshState()

    if (isBatchDownload) {
        BatchDownloadLyrics(
            viewModel = viewModel,
            onDone = { onBatchDownloadState(false) })
    }

    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshing,
        state = refreshState,
        onRefresh = {
            viewModel.isRefreshing = true
            scope.launch {
                viewModel.cachedSongs = null
                viewModel.updateAllSongs(context, viewModel.userSettingsController.sortBy, viewModel.userSettingsController.sortOrder)
                delay(1000) // spinner
                viewModel.isRefreshing = false
            }
        },
        indicator = {
            Indicator(
                state = refreshState,
                isRefreshing = viewModel.isRefreshing,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = scaffoldPadding.calculateTopPadding()),
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = scaffoldPadding
        ) {
            item {
                Column(
                    modifier = Modifier.padding(
                        top = 5.dp,
                        bottom = 5.dp,
                        start = 22.dp,
                        end = 4.dp
                    ),
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
                                onSortClick = { viewModel.showSort = true },
                                onSearchClick = {
                                    viewModel.showSearch = true
                                    viewModel.showingSearch = true
                                }
                            )
                        }
                    )

                    if (viewModel.showSort) {
                        SortDialog(
                            userSettingsController = viewModel.userSettingsController,
                            onDismiss = { viewModel.showSort = false },
                            onSortOrderChange = {
                                viewModel.userSettingsController.updateSortOrder(
                                    it
                                )
                            },
                            onSortByChange = { viewModel.userSettingsController.updateSortBy(it) }
                        )
                    }

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

            item {
                if (viewModel.playingSongTitle.isNotEmpty()) {
                    Text(stringResource(id = R.string.now_playing_song))
                    SongItem(
                        filePath = viewModel.playingSongFilePath,
                        selected = false,
                        quickSelect = false,
                        onSelectionChanged = {},
                        onNavigateToSongRequest = {
                            navController.navigate(
                                LyricsFetchScreen(
                                    songName = viewModel.playingSongTitle,
                                    artists = viewModel.playingSongArtist,
                                    coverUri = viewModel.playingSongAlbumArt.toString(),
                                    filePath = viewModel.playingSongFilePath
                                )
                            )
                        },
                        song = Song(
                            title = viewModel.playingSongTitle,
                            artist = viewModel.playingSongArtist,
                            imgUri = viewModel.playingSongAlbumArt,
                            filePath = viewModel.playingSongFilePath
                        ),
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        disableMarquee = viewModel.userSettingsController.disableMarquee,
                        showPath = viewModel.userSettingsController.showPath
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                }
            }

            items(viewModel.displaySongs.size) { index ->
                val song = viewModel.displaySongs[index]

                SongItem(
                    filePath = song.filePath
                        ?: error("a song in the list of files did not have a file path"),
                    selected = selected.contains(song.filePath),
                    quickSelect = selected.size > 0,
                    onSelectionChanged = { newValue ->
                        viewModel.selectSong(song, newValue)
                    },
                    onNavigateToSongRequest = {
                        navController.navigate(
                            LyricsFetchScreen(
                                songName = song.title ?: error("song.title was null"),
                                artists = song.artist ?: "",
                                coverUri = song.imgUri.toString(),
                                filePath = song.filePath
                            )
                        )
                    },
                    song = song,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    disableMarquee = viewModel.userSettingsController.disableMarquee,
                    showPath = viewModel.userSettingsController.showPath
                )
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}