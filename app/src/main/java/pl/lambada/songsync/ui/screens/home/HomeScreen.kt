package pl.lambada.songsync.ui.screens.home

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.rememberCoroutineScope
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

            BackPressHandler(enabled = viewModel.selected.size > 0, onBackPressed = { viewModel.selected.clear() })
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
        if (viewModel.allSongs == null) {
            LoadingScreen()
        } else {
            HomeScreenLoaded(
                navController = navController,
                viewModel = viewModel,
                selected = viewModel.selected,
                songs = viewModel.allSongs!!,
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
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
    val scope = rememberCoroutineScope()
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
                songs = if (selected.isEmpty()) displaySongs else songs.filter { selected.contains(it.filePath) }.toList(),
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
                    modifier = Modifier
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .padding(
                            top = 5.dp,
                            bottom = 5.dp,
                            start = 22.dp,
                            end = 4.dp
                        ),
                ) {
                    val focusRequester = remember { FocusRequester() }
                    var willShowIme by remember { mutableStateOf(false) }

                    @OptIn(ExperimentalLayoutApi::class)
                    val showingIme = WindowInsets.isImeVisible
                    if (!showingSearch && showSearch) {
                        showingSearch = true
                    }
                    if (!showSearch && !willShowIme && showingSearch && !WindowInsets.isImeVisible && query.text.isEmpty()) {
                        // If search already is no longer "to be shown" but "currently showing", query is
                        // empty and user hides soft-keyboard, we close search bar
                        showingSearch = false
                    }
                    AnimatedContent(
                        targetState = showingSearch, transitionSpec = {
                            if (targetState) {
                                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                    slideOutVertically { height -> -height } + fadeOut())
                            } else {
                                (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                    slideOutVertically { height -> height } + fadeOut())
                            }.using(
                                SizeTransform()
                            )
                        }, label = "", modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    ) { showing ->
                        if (showing) {
                            if (willShowIme && WindowInsets.isImeVisible) {
                                willShowIme = false
                            }
                            val focusManager = LocalFocusManager.current

                            TextField(
                                value = query,
                                onValueChange = {
                                    query = it
                                    viewModel.updateSearchResults(it.text.lowercaseWithLocale())
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = stringResource(id = R.string.search),
                                        modifier = Modifier.clickable {
                                            showSearch = false
                                            showingSearch = false
                                        }
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(id = R.string.clear),
                                        modifier = Modifier.clickable {
                                            query = TextFieldValue("")
                                            viewModel.updateSearchResults("")
                                            showSearch = false
                                            showingSearch = false
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
                                            showSearch = false
                                        }
                                    },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { focusManager.clearFocus() }
                                )
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "${displaySongs.size} songs")
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { showFilters = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterAlt,
                                        contentDescription = stringResource(R.string.search),
                                    )
                                }

                                if (showFilters) {
                                    FiltersDialog(
                                        hideLyrics = viewModel.hideLyrics,
                                        folders = viewModel.getSongFolders(context),
                                        blacklistedFolders = viewModel.blacklistedFolders,
                                        onDismiss = { showFilters = false  },
                                        onFilterChange = { viewModel.filterSongs() },
                                        onHideLyricsChange = { viewModel.onHideLyricsChange(dataStore, it) },
                                        onToggleFolderBlacklist = viewModel::onToggleFolderBlacklist
                                    )
                                }

                                IconButton(onClick = { showSearch = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(R.string.search),
                                    )
                                }
                            }
                        }
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