package pl.lambada.songsync.ui.screens.home

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.parcelize.Parcelize
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.ui.ScreenAbout
import pl.lambada.songsync.ui.ScreenSearch
import pl.lambada.songsync.ui.screens.home.components.BatchDownloadLyrics
import pl.lambada.songsync.ui.screens.home.components.FiltersDialog
import pl.lambada.songsync.ui.screens.home.components.HomeAppBar
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
        AnimatedContent(viewModel.allSongs) { songsList ->
            if (songsList == null)
                LoadingScreen()
            else
                HomeScreenLoaded(
                    navController = navController,
                    viewModel = viewModel,
                    selected = viewModel.selected,
                    songs = songsList,
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

@Parcelize
data class MyTextFieldValue(val text: String, val cursorStart: Int, val cursorEnd: Int) : Parcelable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreenLoaded(
    selected: SnapshotStateList<String>,
    navController: NavHostController,
    viewModel: MainViewModel,
    songs: List<Song>,
    paddingValues: PaddingValues,
    isBatchDownload: Boolean,
    onBatchDownload: (Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    var showingSearch by rememberSaveable { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(showingSearch) }
    var query by rememberSaveable(
        stateSaver = Saver(save = {
            MyTextFieldValue(
                it.text,
                it.selection.start,
                it.selection.end
            )
        },
            restore = { TextFieldValue(it.text, TextRange(it.cursorStart, it.cursorEnd)) })
    ) { mutableStateOf(TextFieldValue()) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val filtered = viewModel.cachedFilteredSongs.collectAsState()
    val searched = viewModel.searchResults.collectAsState()
    val context = LocalContext.current
    val displaySongs = when {
        query.text.isNotEmpty() -> searched.value
        filtered.value.isNotEmpty() -> filtered.value
        else -> songs
    }

    val dataStore = context.dataStore

    LaunchedEffect(Unit) {
        viewModel.filterSongs()
    }

    Column {
        if (isBatchDownload) {
            BatchDownloadLyrics(
                songs = if (selected.isEmpty()) displaySongs else songs.filter {
                    selected.contains(
                        it.filePath
                    )
                }.toList(),
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
                    modifier = Modifier.padding(
                        top = 5.dp,
                        bottom = 5.dp,
                        start = 22.dp,
                        end = 4.dp
                    ),
                ) {
                    HomeSearchThing(
                        showingSearch = showingSearch,
                        searchBar = {
                            HomeSearchBar(
                                query = query,
                                onQueryChange = { newQuery ->
                                    query = newQuery
                                    viewModel.updateSearchResults(newQuery.text.lowercaseWithLocale())
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
                                displaySongsCount = displaySongs.size,
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
                            onToggleFolderBlacklist = viewModel::onToggleFolderBlacklist
                        )
                    }
                }
            }

            items(displaySongs.size) { i ->
                val song = displaySongs[i]

                SongItem(
                    id = i.toString(),
                    selected = selected.contains(song.filePath),
                    quickSelect = selected.size > 0,
                    onSelectionChanged = { newValue ->
                        if (newValue) {
                            song.filePath?.let { selected.add(it) }
                            showSearch = false
                            showingSearch = false
                        } else {
                            selected.remove(song.filePath)
                            if (selected.size == 0 && query.text.isNotEmpty()) showingSearch =
                                true // show again but don't focus
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

@Composable
fun HomeSearchThing(
    showingSearch: Boolean,
    searchBar: @Composable () -> Unit,
    filterBar: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = showingSearch,
        transitionSpec = {
            if (targetState) {
                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height } + fadeOut()
                )
            } else {
                (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> height } + fadeOut()
                )
            }.using(
                SizeTransform()
            )
        },
        label = "",
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
    ) { showing ->
        if (showing) searchBar() else filterBar()
    }
}

@Composable
fun HomeSearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    showSearch: Boolean,
    onShowSearchChange: (Boolean) -> Unit,
    showingSearch: Boolean,
    onShowingSearchChange: (Boolean) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var willShowIme by remember { mutableStateOf(false) }

    @OptIn(ExperimentalLayoutApi::class)
    val showingIme = WindowInsets.isImeVisible

    if (!showingSearch && showSearch) {
        onShowingSearchChange(true)
    }

    if (!showSearch && !willShowIme && showingSearch && !showingIme && query.text.isEmpty()) {
        onShowingSearchChange(false)
    }

    if (willShowIme && showingIme) {
        willShowIme = false
    }
    val focusManager = LocalFocusManager.current

    TextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search),
                modifier = Modifier.clickable {
                    onShowSearchChange(false)
                    onShowingSearchChange(false)
                }
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(id = R.string.clear),
                modifier = Modifier.clickable {
                    onQueryChange(TextFieldValue(""))
                    onShowSearchChange(false)
                    onShowingSearchChange(false)
                }
            )
        },
        placeholder = { Text(stringResource(id = R.string.search)) },
        shape = ShapeDefaults.ExtraLarge,
        colors = TextFieldDefaults.colors(
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .padding(end = 18.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused && !showingIme) willShowIme = true
            }
            .onGloballyPositioned {
                if (showSearch && !showingIme) {
                    focusRequester.requestFocus()
                    onShowSearchChange(false)
                }
            },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() }
        )
    )
}

@Composable
fun FilterAndSongCount(
    displaySongsCount: Int,
    onFilterClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = "$displaySongsCount songs")
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Outlined.FilterAlt,
                contentDescription = stringResource(R.string.search),
            )
        }

        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
            )
        }
    }
}