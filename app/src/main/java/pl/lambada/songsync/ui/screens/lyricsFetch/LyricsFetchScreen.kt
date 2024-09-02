package pl.lambada.songsync.ui.screens.lyricsFetch

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.LocalSong
import pl.lambada.songsync.ui.components.CommonTextField
import pl.lambada.songsync.ui.components.SongCard
import pl.lambada.songsync.ui.screens.about.AboutViewModel
import pl.lambada.songsync.ui.screens.about.Providers
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.NoTrackFoundException
import java.io.FileNotFoundException

/**
 * Composable function for BrowseScreen component.
 *
 * @param viewModel the [AboutViewModel] instance.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LyricsFetchScreen(
    viewModel: LyricsFetchViewModel,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        if (viewModel.source != null) viewModel.loadSongInfo(context)
    }

    Scaffold(
        modifier = Modifier
            .sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = if (viewModel.source == null) "fab" else ""
                ),
                animatedVisibilityScope = animatedVisibilityScope
            )
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack(
                                navController.graph.startDestinationId,
                                false
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.search),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.source != null) {
                LocalSongContent(
                    viewModel.source,
                    animatedVisibilityScope = animatedVisibilityScope,
                    disableMarquee = viewModel.userSettingsController.disableMarquee
                )
            }

            AnimatedContent(viewModel.queryState, label = "") { queryState ->
                when (queryState) {
                    QueryStatus.NotSubmitted -> NotSubmittedContent(
                        querySong = viewModel.querySongName,
                        onQuerySongChange = { viewModel.querySongName = it },
                        queryArtist = viewModel.queryArtistName,
                        onQueryArtistChange = { viewModel.queryArtistName = it },
                        onGetLyricsRequest = { viewModel.loadSongInfo(context) }
                    )

                    QueryStatus.Pending -> Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }

                    is QueryStatus.Success -> SuccessContent(
                        result = queryState.song,
                        onTryAgain = { viewModel.loadSongInfo(context, true) },
                        onEdit = { viewModel.queryState = QueryStatus.NotSubmitted },
                        onSaveLyrics = {
                            viewModel.saveLyricsToFile(
                                it,
                                queryState.song,
                                viewModel.source?.filePath,
                                context,
                                context.getString(R.string.generated_using)
                            )
                        },
                        onEmbedLyrics = {
                            viewModel.embedLyricsInFile(
                                it,
                                viewModel.source?.filePath,
                                context,
                                context.getString(R.string.generated_using),
                                queryState.song
                            )
                        },
                        onCopyLyrics = {
                            clipboardManager.setText(AnnotatedString(it))

                            Toast.makeText(
                                context,
                                context.getString(R.string.lyrics_copied_to_clipboard),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        openUri = uriHandler::openUri,
                        lyricsFetchState = viewModel.lyricsFetchState,
                        animatedVisibilityScope = animatedVisibilityScope,
                        disableMarquee = viewModel.userSettingsController.disableMarquee,
                        allowTryingAgain = viewModel.userSettingsController.selectedProvider != Providers.LRCLIB
                                && viewModel.userSettingsController.selectedProvider != Providers.APPLE
                    )

                    is QueryStatus.Failed -> FailedDialogue(
                        onDismissRequest = { viewModel.queryState = QueryStatus.NotSubmitted },
                        onOkRequest = { viewModel.queryState = QueryStatus.NotSubmitted },
                        exception = queryState.exception
                    )

                    QueryStatus.NoConnection -> NoConnectionDialogue(
                        onDismissRequest = { viewModel.queryState = QueryStatus.NotSubmitted },
                        onOkRequest = { viewModel.queryState = QueryStatus.NotSubmitted }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.SuccessContent(
    result: SongInfo,
    onTryAgain: () -> Unit,
    onEdit: () -> Unit,
    onSaveLyrics: (String) -> Unit,
    onEmbedLyrics: (String) -> Unit,
    onCopyLyrics: (String) -> Unit,
    openUri: (String) -> Unit,
    lyricsFetchState: LyricsFetchState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    disableMarquee: Boolean,
    allowTryingAgain: Boolean,
) = Column {
    Spacer(modifier = Modifier.height(10.dp))

    Row {
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = null,
            Modifier.padding(end = 5.dp)
        )
        Text(stringResource(R.string.cloud_song))
    }

    Spacer(modifier = Modifier.height(6.dp))

    SongCard(
        filePath = null, // maybe should make a sealed class to divide song data fetched online or offline by types
        songName = result.songName ?: stringResource(id = R.string.unknown),
        artists = result.artistName ?: stringResource(id = R.string.unknown),
        coverUrl = result.albumCoverLink,
        modifier = Modifier.clickable { result.songLink?.let(openUri) },
        animatedVisibilityScope = animatedVisibilityScope,
        animateText = !disableMarquee,
    )

    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            enabled = allowTryingAgain,
            onClick = onTryAgain
        ) {
            Text(text = stringResource(id = R.string.try_again))
        }
        OutlinedButton(
            onClick = onEdit
        ) {
            Text(text = stringResource(id = R.string.edit))
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    when (lyricsFetchState) {
        LyricsFetchState.NotSubmitted -> CircularProgressIndicator()

        is LyricsFetchState.Success -> LyricsSuccessContent(
            lyrics = lyricsFetchState.lyrics,
            onSaveLyrics = onSaveLyrics,
            onEmbedLyrics = onEmbedLyrics,
            onCopyLyrics = { onCopyLyrics(lyricsFetchState.lyrics) }
        )

        is LyricsFetchState.Failed -> {
            Text(
                text = lyricsFetchState.exception.stackTraceToString()
                    ?: stringResource(id = R.string.this_track_has_no_lyrics)
            )
        }

        LyricsFetchState.Pending -> CircularProgressIndicator()
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun LyricsSuccessContent(
    lyrics: String,
    onSaveLyrics: (String) -> Unit,
    onEmbedLyrics: (String) -> Unit,
    onCopyLyrics: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { onSaveLyrics(lyrics) }) {
            Text(text = stringResource(R.string.save_lrc_file))
        }
        Button(onClick = { onEmbedLyrics(lyrics) }) {
            Text(text = stringResource(R.string.embed_lyrics_in_file))
        }
    }

    OutlinedButton(onClick = onCopyLyrics) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = stringResource(R.string.copy_lyrics_to_clipboard)
        )
    }

    Spacer(modifier = Modifier.height(6.dp))
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        SelectionContainer {
            Text(text = lyrics, modifier = Modifier.padding(8.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LocalSongContent(
    song: LocalSong,
    animatedVisibilityScope: AnimatedVisibilityScope,
    disableMarquee: Boolean
) {
    Row {
        Icon(
            imageVector = Icons.Filled.Downloading,
            contentDescription = null,
            Modifier.padding(end = 5.dp)
        )
        Text(stringResource(R.string.local_song))
    }
    Spacer(modifier = Modifier.height(6.dp))
    SongCard(
        filePath = song.filePath,
        songName = song.songName,
        artists = song.artists,
        coverUrl = song.coverUri,
        animatedVisibilityScope = animatedVisibilityScope,
        animateText = !disableMarquee,
    )
}

@Composable
fun FailedDialogue(
    onDismissRequest: () -> Unit,
    onOkRequest: () -> Unit,
    exception: Exception
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { Button(onOkRequest) { Text(stringResource(R.string.ok)) } },
        title = { Text(text = stringResource(id = R.string.error)) },
        text = {
            when (exception) {
                is NoTrackFoundException -> Text(stringResource(R.string.no_results))

                is EmptyQueryException -> Text(stringResource(R.string.invalid_query))

                is FileNotFoundException -> { // Rate limit
                    Column {
                        Text(text = stringResource(R.string.spotify_api_rate_limit_reached))
                        Text(text = stringResource(R.string.please_try_again_later))
                        Text(text = stringResource(R.string.change_api_strategy))
                    }
                }

                else -> Text(exception.toString())
            }
        }
    )
}

@Composable
fun NoConnectionDialogue(
    onDismissRequest: () -> Unit,
    onOkRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { Button(onOkRequest) { Text(stringResource(R.string.ok)) } },
        title = { Text(text = stringResource(id = R.string.error)) },
        text = { Text(stringResource(R.string.no_internet_server)) }
    )
}

@Composable
fun NotSubmittedContent(
    querySong: String,
    onQuerySongChange: (String) -> Unit,
    queryArtist: String,
    onQueryArtistChange: (String) -> Unit,
    onGetLyricsRequest: () -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        CommonTextField(
            value = querySong,
            onValueChange = onQuerySongChange,
            label = stringResource(id = R.string.song_name_no_args),
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        CommonTextField(
            value = queryArtist,
            onValueChange = onQueryArtistChange,
            label = stringResource(R.string.artist_name_no_args),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onGetLyricsRequest) {
            Text(text = stringResource(id = R.string.get_lyrics))
        }
    }
}

