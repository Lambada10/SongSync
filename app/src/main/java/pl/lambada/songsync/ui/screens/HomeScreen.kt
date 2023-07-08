package pl.lambada.songsync.ui.screens

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.dto.Song
import pl.lambada.songsync.data.dto.SongInfo
import pl.lambada.songsync.data.ext.lowercaseWithLocale
import pl.lambada.songsync.ui.Screens
import pl.lambada.songsync.ui.components.MarqueeText
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.roundToInt

/**
 * Composable function representing the home screen.
 *
 * @param viewModel The [MainViewModel] instance.
 */
@Composable
fun HomeScreen(navController: NavHostController, viewModel: MainViewModel) {
    var uiState by remember { mutableStateOf(UiState.Loading) }
    val context = LocalContext.current
    var songs by remember { mutableStateOf(emptyList<Song>()) }

    when (uiState) {
        UiState.Loading -> {
            LoadingScreen(onLoadingComplete = {
                songs = viewModel.getAllSongs(context)
                uiState = UiState.Loaded
            })
        }

        UiState.Loaded -> HomeScreenLoaded(
            navController = navController,
            viewModel = viewModel,
            songs = songs
        )

        else -> {
            Text(text = stringResource(id = R.string.unreachable_state))
        }
    }
}

/**
 * Composable function representing the loading screen.
 *
 * @param onLoadingComplete Callback invoked when the loading is complete.
 */
@Composable
fun LoadingScreen(onLoadingComplete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        LaunchedEffect(Unit) {
            onLoadingComplete()
        }
    }
}

@Parcelize
data class MyTextFieldValue(val text: String, val cursorStart: Int, val cursorEnd: Int) : Parcelable

@OptIn(
    ExperimentalLayoutApi::class
)
@Composable
fun HomeScreenLoaded(navController: NavHostController, viewModel: MainViewModel, songs: List<Song>) {
    var showingSearch by rememberSaveable { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(showingSearch) }
    var query by rememberSaveable(stateSaver = Saver(
        save = { MyTextFieldValue(it.text, it.selection.start, it.selection.end) },
        restore = { TextFieldValue(it.text, TextRange(it.cursorStart, it.cursorEnd)) })
    ) { mutableStateOf(TextFieldValue()) }
    var isBatchDownload by rememberSaveable { mutableStateOf(false) }

    Column {
        if (isBatchDownload) {
            BatchDownloadLyrics(
                songs = songs,
                viewModel = viewModel,
                onDone = { isBatchDownload = false }
            )
        }
        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
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
                targetState = showingSearch,
                transitionSpec = {
                    // Compare the incoming number with the previous number.
                    if (targetState) {
                        // If the target number is larger, it slides up and fades in
                        // while the initial (smaller) number slides up and fades out.
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        // If the target number is smaller, it slides down and fades in
                        // while the initial number slides down and fades out.
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut())
                    }.using(
                        SizeTransform()
                    )
                },
                label = "",
                modifier = Modifier.fillMaxWidth()
                    .height(55.dp)
                    .padding(horizontal = 20.dp)
            ) { showing ->
                if (showing) {
                    if (willShowIme && WindowInsets.isImeVisible) {
                        willShowIme = false
                    }
                    val focusManager = LocalFocusManager.current
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(id = R.string.search)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = stringResource(id = R.string.search),
                                modifier = Modifier.clickable {
                                    query = TextFieldValue("")
                                    showSearch = false
                                    showingSearch = false
                                }
                            )
                        },
                        shape = ShapeDefaults.ExtraLarge,
                        colors = TextFieldDefaults.colors(
                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                    Row(Modifier, verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { isBatchDownload = true }) {
                            Text(text = stringResource(R.string.batch_download_lyrics))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { showSearch = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(songs.size) {
                val song = songs[it]
                val songTitleLowercase = song.title?.lowercaseWithLocale()
                val songArtistLowercase = song.artist?.lowercaseWithLocale()
                val queryLowercase = query.text.lowercaseWithLocale()

                if (songTitleLowercase?.contains(queryLowercase) == true || songArtistLowercase?.contains(
                        queryLowercase
                    ) == true
                ) {
                    SongItem(navController = navController, song = song, viewModel = viewModel)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SongItem(navController: NavHostController, song: Song, viewModel: MainViewModel) {
    OutlinedCard(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                viewModel.nextSong = song
                navController.navigate(Screens.Browse.name)
            }
    ) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(data = song.imgUri)
                .apply {
                    placeholder(0)
                }.build(),
            imageLoader = LocalContext.current.imageLoader
        )

        Row(modifier = Modifier.height(72.dp)) {
            Image(
                painter = painter,
                contentDescription = stringResource(id = R.string.album_cover),
                modifier = Modifier
                    .height(72.dp)
                    .aspectRatio(1f),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Top) {
                MarqueeText(text = song.title ?: stringResource(id = R.string.unknown), fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                MarqueeText(text = song.artist ?: stringResource(id = R.string.unknown), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BatchDownloadLyrics(songs: List<Song>, viewModel: MainViewModel, onDone: () -> Unit) {
    val unknownString = stringResource(id = R.string.unknown)
    val generatedUsingString = stringResource(id = R.string.generated_using)

    var uiState by rememberSaveable { mutableStateOf(UiState.Warning) }
    var failedCount by rememberSaveable { mutableIntStateOf(0) }
    var successCount by rememberSaveable { mutableIntStateOf(0) }
    val total = songs.size

    when (uiState) {
        UiState.Cancelled -> {
            onDone()
        }

        UiState.Warning -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.this_will_download_lyrics_for_all_songs))
                        Text(text = stringResource(R.string.existing_lyrics_for_songs_overwrite))
                        Text(text = stringResource(R.string.less_accurate))
                        Text(text = stringResource(R.string.sure_to_continue))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(onClick = { uiState = UiState.Pending }) {
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

        UiState.Pending -> {
            val totalCount = successCount + failedCount
            val percentage = if (total != 0) {
                (totalCount.toFloat() / total.toFloat() * 100).roundToInt()
            } else {
                0 // In other cases = 0
            }

            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.downloading_lyrics))
                        MarqueeText(
                            stringResource(
                                R.string.song,
                                songs[(successCount + failedCount) % total].title ?: unknownString,
                            )
                        )
                        Text(text = stringResource(R.string.progress, totalCount, total, percentage))
                        Text(text = stringResource(R.string.success_failed, successCount, failedCount))
                        Text(text = stringResource(R.string.please_do_not_close_the_app_this_may_take_a_while))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
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
                    for (i in (failedCount + successCount) until songs.size) {
                        val song = songs[i]
                        if (uiState == UiState.Cancelled) {
                            downloadJob?.cancel()
                            return@launch
                        }

                        if (successCount + failedCount >= total) {
                            uiState = UiState.Done
                            downloadJob?.cancel()
                            return@launch
                        }

                        val query = SongInfo(song.title, song.artist)
                        var queryResult: SongInfo? = null
                        try {
                            queryResult = viewModel.getSongInfo(query)
                        } catch (e: Exception) {
                            if (e is FileNotFoundException) {
                                notFoundInARow++
                                failedCount++
                                if (notFoundInARow >= 5) {
                                    uiState = UiState.RateLimited
                                    return@launch
                                }
                                continue
                            }
                            if (e is JSONException) {
                                failedCount++
                                continue
                            }
                        } finally {
                            notFoundInARow = 0
                        }
                        val lyricsResult: String
                        try {
                            lyricsResult = viewModel.getSyncedLyrics(queryResult?.songLink!!)!!
                        } catch (e: Exception) {
                            when (e) {
                                is NullPointerException, is FileNotFoundException -> {
                                    failedCount++
                                    continue
                                }
                                else -> throw e
                            }
                        }
                        val lrc =
                            "[ti:${queryResult.songName}]\n" +
                                    "[ar:${queryResult.artistName}]\n" +
                                    "[by:$generatedUsingString]\n" +
                                    lyricsResult

                        val file = song.let {
                            val filePath = it.filePath!!
                            val idx = filePath.lastIndexOf('.')
                            File(filePath.substring(0, if (idx == -1) filePath.length else idx) + ".lrc")
                        }
                        file.writeText(lrc)

                        successCount++
                    }
                    uiState = UiState.Done
                }
            }
        }

        UiState.Done -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.download_complete))
                        Text(text = stringResource(R.string.success, successCount))
                        Text(text = stringResource(R.string.failed, failedCount))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            )
        }

        UiState.RateLimited -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.spotify_api_rate_limit_reached))
                        Text(text = stringResource(R.string.please_try_again_later))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            )
        }

        else -> {
            // nothing because we do not use the other states
        }
    }
}

enum class UiState {
    Warning, Pending, Done, RateLimited, Cancelled, Loading, Loaded
}