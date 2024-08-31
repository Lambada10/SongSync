package pl.lambada.songsync.ui.screens

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getString
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import pl.lambada.songsync.MainActivity
import pl.lambada.songsync.R
import pl.lambada.songsync.data.EmptyQueryException
import pl.lambada.songsync.data.InternalErrorException
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.NoTrackFoundException
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.ScreenAbout
import pl.lambada.songsync.ui.ScreenSearch
import pl.lambada.songsync.util.ext.BackPressHandler
import pl.lambada.songsync.ui.components.AnimatedText
import pl.lambada.songsync.ui.components.SwitchItem
import pl.lambada.songsync.util.dataStore
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.ext.lowercaseWithLocale
import pl.lambada.songsync.util.ext.toLrcFile
import pl.lambada.songsync.util.set
import java.io.FileNotFoundException
import kotlin.math.roundToInt

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
            var ableToSelect by remember { mutableStateOf<List<Song>?>(null) }

            val searched = viewModel.searchResults.collectAsState()
            val filtered = viewModel.cachedFilteredSongs.collectAsState()

            ableToSelect = when {
                searched.value.isNotEmpty() -> searched.value
                filtered.value.isNotEmpty() -> filtered.value
                else -> viewModel.allSongs
            }

            BackPressHandler(enabled = viewModel.selected.size > 0, onBackPressed = { viewModel.selected.clear() })
            Crossfade(
                targetState = viewModel.selected.size > 0,
                label = ""
            ) { showing ->
                MediumTopAppBar(
                    navigationIcon = {
                        if (showing) {
                            IconButton(onClick = { viewModel.selected.clear() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
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
                                    modifier = Modifier.padding(start = 6.dp),
                                    text = stringResource(id = R.string.selected_count, size)
                                )
                            }
                        } else {
                            Text(
                                modifier = Modifier.padding(start = 6.dp),
                                text = stringResource(R.string.app_name)
                            )
                        }
                    },
                    actions = {
                        if (showing) {
                            IconButton(
                                onClick = {
                                    viewModel.selected.clear()
                                    ableToSelect?.map { it.filePath }?.forEach {
                                        if (it != null) {
                                            viewModel.selected.add(it)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SelectAll,
                                    contentDescription = stringResource(R.string.select_all)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val willBeSelected =
                                        ableToSelect?.map { it.filePath }?.toMutableList()
                                    for (song in viewModel.selected) {
                                        willBeSelected?.remove(song)
                                    }
                                    viewModel.selected.clear()
                                    if (willBeSelected != null) {
                                        for (song in willBeSelected) {
                                            song?.let { viewModel.selected.add(it) }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Deselect,
                                    contentDescription = stringResource(
                                        id = R.string.invert_selection
                                    )
                                )
                            }
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.batch_download_lyrics),
                                            modifier = Modifier.padding(horizontal = 6.dp),
                                        )
                                    },
                                    onClick = {
                                        isBatchDownload = true
                                        expanded = false
                                    }
                                )
                            }
                        } else {
                            var expanded by remember { mutableStateOf(false) }
                            var expandedProviders by remember { mutableStateOf(false) }
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.provider),
                                            modifier = Modifier.padding(horizontal = 6.dp),
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                                            contentDescription = "expand dropdown"
                                        )
                                    },
                                    onClick = {
                                        expanded = false
                                        expandedProviders = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.batch_download_lyrics),
                                            modifier = Modifier.padding(horizontal = 6.dp),
                                        )
                                    },
                                    onClick = {
                                        isBatchDownload = true
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.about),
                                            modifier = Modifier.padding(horizontal = 6.dp),
                                        )
                                    },
                                    onClick = {
                                        navController.navigate(ScreenAbout)
                                        expanded = false
                                    }
                                )
                            }
                            val selectedProvider = rememberSaveable { mutableStateOf(viewModel.provider) }
                            val providers = Providers.entries.toTypedArray()
                            val context = LocalContext.current
                            val dataStore = context.dataStore
                            DropdownMenu(
                                expanded = expandedProviders,
                                onDismissRequest = { expandedProviders = false }
                            ) {
                                Text(
                                    text = stringResource(id = R.string.provider),
                                    modifier = Modifier.padding(start = 18.dp, top = 8.dp),
                                    fontSize = 12.sp
                                )
                                providers.forEach {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = it.displayName,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(start = 6.dp)
                                                )
                                                RadioButton(
                                                    selected = selectedProvider.value == it,
                                                    onClick = {
                                                        selectedProvider.value = it
                                                        viewModel.provider = it
                                                        dataStore.set(
                                                            stringPreferencesKey("provider"),
                                                            it.displayName
                                                        )
                                                        expandedProviders = false
                                                    }
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedProvider.value = it
                                            viewModel.provider = it
                                            dataStore.set(
                                                stringPreferencesKey("provider"),
                                                it.displayName
                                            )
                                            expandedProviders = false
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
                songs = viewModel.allSongs,
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
                            TextField(value = query,
                                onValueChange = {
                                    query = it
                                    viewModel.updateSearchResults(it.text.lowercaseWithLocale())
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search,
                                        contentDescription = stringResource(id = R.string.search),
                                        modifier = Modifier.clickable {
                                            showSearch = false
                                            showingSearch = false
                                        })
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
                                        if (it.isFocused && !showingIme) {
                                            willShowIme = true
                                        }
                                    }
                                    .onGloballyPositioned {
                                        if (showSearch && !showingIme) {
                                            focusRequester.requestFocus()
                                            showSearch = false
                                        }
                                    },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    focusManager.clearFocus()
                                })
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
                                        viewModel = viewModel,
                                        context = context,
                                        onDismiss = { showFilters = false },
                                        onFilterChange = {
                                            scope.launch(Dispatchers.Default) {
                                                viewModel.filterSongs()
                                            }
                                        },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersDialog(
    viewModel: MainViewModel,
    context: Context,
    onDismiss: () -> Unit,
    onFilterChange: () -> Unit
) {
    var hideLyrics by remember { mutableStateOf(viewModel.hideLyrics) }
    val folders = viewModel.getSongFolders(context)
    var showFolders by rememberSaveable { mutableStateOf(false) }

    val dataStore = context.dataStore

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
                    viewModel.hideLyrics = !hideLyrics
                    dataStore.set(
                        booleanPreferencesKey("hide_lyrics"),
                        !hideLyrics
                    )
                    hideLyrics = !hideLyrics
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
                        items(folders.size) {
                            val folder = folders[it]
                            var checked by remember {
                                mutableStateOf(viewModel.blacklistedFolders.contains(folder))
                            }

                            Row(
                                modifier = Modifier
                                    .clickable {
                                        checked = !checked
                                        if (checked) {
                                            viewModel.blacklistedFolders.add(folder)
                                        } else {
                                            viewModel.blacklistedFolders.remove(folder)
                                        }
                                        dataStore.set(
                                            stringPreferencesKey("blacklist"),
                                            viewModel.blacklistedFolders.joinToString(",")
                                        )
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
                                        if (check) {
                                            viewModel.blacklistedFolders.add(folder)
                                        } else {
                                            viewModel.blacklistedFolders.remove(folder)
                                        }
                                        dataStore.set(
                                            stringPreferencesKey("blacklist"),
                                            viewModel.blacklistedFolders.joinToString(",")
                                        )
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SongItem(
    id: String,
    selected: Boolean,
    quickSelect: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    navController: NavHostController,
    song: Song,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: MainViewModel,
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current).data(data = song.imgUri).apply {
            placeholder(R.drawable.ic_song)
            error(R.drawable.ic_song)
        }.build(), imageLoader = LocalContext.current.imageLoader
    )
    val songName = song.title ?: stringResource(id = R.string.unknown)
    val artists = song.artist ?: stringResource(id = R.string.unknown)
    val bgColor = if (selected) MaterialTheme.colorScheme.surfaceVariant
    else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(bgColor)
            .combinedClickable(
                onClick = {
                    if (quickSelect) {
                        onSelectionChanged(!selected)
                    } else {
                        navController.navigate(
                            ScreenSearch(
                                id = id,
                                songName = songName,
                                artists = artists,
                                coverUri = song.imgUri.toString(),
                                filePath = song.filePath,
                            )
                        )
                    }
                },
                onLongClick = { onSelectionChanged(!selected) }
            )
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        with(sharedTransitionScope) {
            Image(
                painter = painter,
                contentDescription = stringResource(id = R.string.album_cover),
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "cover$id"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        clipInOverlayDuringTransition = sharedTransitionScope.OverlayClip(
                            RoundedCornerShape(20f)
                        )
                    )
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                AnimatedText(
                    animate = !viewModel.disableMarquee.value,
                    text = songName,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.contentColorFor(bgColor),
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "title$id"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
                AnimatedText(
                    animate = !viewModel.disableMarquee.value,
                    text = artists,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.contentColorFor(bgColor),
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "artist$id"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            }
        }
    }
}

@SuppressLint("StringFormatMatches")
@Composable
fun BatchDownloadLyrics(songs: List<Song>, viewModel: MainViewModel, onDone: () -> Unit) {
    val unknownString = stringResource(id = R.string.unknown)
    val generatedUsingString = stringResource(id = R.string.generated_using)

    var uiState by rememberSaveable { mutableStateOf(UiState.Warning) }
    var failedCount by rememberSaveable { mutableIntStateOf(0) }
    var noLyricsCount by rememberSaveable { mutableIntStateOf(0) }
    var successCount by rememberSaveable { mutableIntStateOf(0) }
    val count = successCount + failedCount + noLyricsCount
    val total = songs.size
    val isLegacyVersion = Build.VERSION.SDK_INT < Build.VERSION_CODES.R

    val context = LocalContext.current
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelID = getString(context, R.string.batch_download_lyrics)

    val resultIntent = Intent(context, MainActivity::class.java)
    resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    resultIntent.setAction(Intent.ACTION_MAIN)
    resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val pendingIntent =
        PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE)

    when (uiState) {
        UiState.Cancelled -> {
            notificationManager.cancelAll()
            onDone()
        }

        UiState.Warning -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(
                            text = pluralStringResource(
                                R.plurals.this_will_download_lyrics_for_all_songs,
                                songs.size,
                                songs.size
                            )
                        )
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(
                        onClick = {
                            uiState = if (isLegacyVersion) {
                                UiState.LegacyPrompt
                            } else {
                                UiState.Pending
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(R.string.no))
                    }
                }
            )
        }

        UiState.LegacyPrompt -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.set_sd_path_warn))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(
                        onClick = {
                            uiState = UiState.Pending
                        },
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            uiState = UiState.Cancelled
                        },
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                },
            )
        }

        UiState.Pending -> {
            val percentage = if (total != 0) {
                (count.toFloat() / total.toFloat() * 100).roundToInt()
            } else {
                0 // In other cases = 0
            }

            val notificationBuilder = NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.downloading_lyrics))
                .setContentText(context.getString(R.string.progress, count, total, percentage))
                .setProgress(100, percentage, false).setTimeoutAfter(2000)
                .setContentIntent(pendingIntent)

            notificationManager.notify(1, notificationBuilder.build())

            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.downloading_lyrics))
                        AnimatedText(
                            animate = !viewModel.disableMarquee.value,
                            text = stringResource(
                                R.string.song,
                                songs.getOrNull((count) % total.coerceAtLeast(1))?.title
                                    ?: unknownString,
                            )
                        )
                        Text(text = stringResource(R.string.progress, count, total, percentage))
                        Text(
                            text = stringResource(
                                R.string.success_failed, successCount, noLyricsCount, failedCount
                            )
                        )
                        Text(text = stringResource(R.string.please_do_not_close_the_app_this_may_take_a_while))
                    }
                },
                onDismissRequest = {
                    /*
                   it's easy to accidentally dismiss the dialog, and since it's a long running task
                   we don't want to accidentally cancel it, so we don't allow dismissing the dialog
                   user can cancel the task by pressing the cancel button
                 */
                },
                confirmButton = {
                    // no button but compose cries when I don't use confirmButton
                },
                dismissButton = {
                    OutlinedButton(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )

            var notFoundInARow by rememberSaveable { mutableIntStateOf(0) }
            var downloadJob by remember { mutableStateOf<Job?>(null) }

            LaunchedEffect(Unit) {
                downloadJob = launch(Dispatchers.IO) {
                    for (i in count until songs.size) {
                        val song = songs[i]
                        if (uiState == UiState.Cancelled) {
                            downloadJob?.cancel()
                            return@launch
                        }

                        if (count >= total) {
                            uiState = UiState.Done
                            downloadJob?.cancel()
                            return@launch
                        }
                        val file = song.filePath.toLrcFile()
                        val query = SongInfo(song.title, song.artist)
                        var queryResult: SongInfo? = null
                        try {
                            queryResult = viewModel.getSongInfo(query)
                        } catch (e: Exception) {
                            when (e) {
                                is FileNotFoundException -> {
                                    notFoundInARow++
                                    failedCount++
                                    if (notFoundInARow >= 5) {
                                        uiState = UiState.RateLimited
                                        return@launch
                                    }
                                    continue
                                }

                                is NoTrackFoundException, is EmptyQueryException, is InternalErrorException -> {
                                    // not increasing notFoundInARow because that is for rate limit
                                    failedCount++
                                }

                                else -> throw e
                            }
                        }
                        notFoundInARow = 0
                        if (queryResult != null) {
                            val lyricsResult: String
                            try {
                                lyricsResult = viewModel.getSyncedLyrics(queryResult.songLink ?: "", context.getVersion())!!
                            } catch (e: Exception) {
                                when (e) {
                                    is NullPointerException, is FileNotFoundException -> {
                                        noLyricsCount++
                                        continue
                                    }

                                    else -> throw e
                                }
                            }
                            val lrc =
                                "[ti:${queryResult.songName}]\n" + "[ar:${queryResult.artistName}]\n" + "[by:$generatedUsingString]\n" + lyricsResult
                            try {
                                file?.writeText(lrc)
                            } catch (e: FileNotFoundException) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !song.filePath!!.contains(
                                        "/storage/emulated/0"
                                    )
                                ) {
                                    val sd = context.externalCacheDirs[1].absolutePath.substring(
                                        0,
                                        context.externalCacheDirs[1].absolutePath.indexOf("/Android/data")
                                    )
                                    val path = file?.absolutePath?.substringAfter(sd)?.split("/")
                                        ?.dropLast(1)
                                    var sdCardFiles = DocumentFile.fromTreeUri(
                                        context, Uri.parse(viewModel.sdCardPath)
                                    )
                                    for (element in path!!) {
                                        for (sdCardFile in sdCardFiles!!.listFiles()) {
                                            if (sdCardFile.name == element) {
                                                sdCardFiles = sdCardFile
                                            }
                                        }
                                    }
                                    sdCardFiles?.listFiles()?.forEach {
                                        if (it.name == file.name) {
                                            it.delete()
                                            return@forEach
                                        }
                                    }
                                    sdCardFiles?.createFile(
                                        "text/lrc", file.name
                                    )?.let {
                                        val outputStream = context.contentResolver.openOutputStream(it.uri)
                                        outputStream?.write(lrc.toByteArray())
                                        outputStream?.close()
                                    }
                                } else {
                                    throw e
                                }
                            }
                            successCount++
                        }
                    }
                    uiState = UiState.Done
                }
            }
        }

        UiState.Done -> {
            notificationManager.cancelAll()

            val notificationBuilder = NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.download_complete)).setContentText(
                    context.getString(
                        R.string.success_failed, successCount, noLyricsCount, failedCount
                    )
                ).setContentIntent(pendingIntent)

            notificationManager.notify(2, notificationBuilder.build())

            AlertDialog(title = {
                Text(text = stringResource(id = R.string.batch_download_lyrics))
            }, text = {
                Column {
                    Text(text = stringResource(R.string.download_complete))
                    Text(text = stringResource(R.string.success, successCount))
                    Text(text = stringResource(R.string.no_lyrics, noLyricsCount))
                    Text(text = stringResource(R.string.failed, failedCount))
                }
            }, onDismissRequest = {
                uiState = UiState.Cancelled
            }, confirmButton = {
                Button(onClick = { uiState = UiState.Cancelled }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            })
        }

        UiState.RateLimited -> {
            notificationManager.cancelAll()

            AlertDialog(title = {
                Text(text = stringResource(id = R.string.batch_download_lyrics))
            }, text = {
                Column {
                    Text(text = stringResource(R.string.spotify_api_rate_limit_reached))
                    Text(text = stringResource(R.string.please_try_again_later))
                    Text(text = stringResource(R.string.change_api_strategy))
                }
            }, onDismissRequest = {
                uiState = UiState.Cancelled
            }, confirmButton = {
                Button(onClick = { uiState = UiState.Cancelled }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            })
        }
    }
}

enum class UiState {
    Warning, LegacyPrompt, Pending, Done, RateLimited, Cancelled
}