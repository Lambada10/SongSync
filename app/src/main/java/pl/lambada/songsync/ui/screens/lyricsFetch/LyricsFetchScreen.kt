package pl.lambada.songsync.ui.screens.lyricsFetch

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.screens.lyricsFetch.components.FailedDialogue
import pl.lambada.songsync.ui.screens.lyricsFetch.components.LocalSongContent
import pl.lambada.songsync.ui.screens.lyricsFetch.components.NoConnectionDialogue
import pl.lambada.songsync.ui.screens.lyricsFetch.components.NotSubmittedContent
import pl.lambada.songsync.ui.screens.lyricsFetch.components.SuccessContent
import pl.lambada.songsync.util.Providers
import pl.lambada.songsync.util.showToast

/**
 * Composable function for BrowseScreen component.
 *
 * @param viewModel the [LyricsFetchState] instance.
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

            Crossfade(
                viewModel.queryState,
                label = ""
            ) { queryState ->
                when (queryState) {
                    QueryStatus.NotSubmitted -> NotSubmittedContent(
                        querySong = viewModel.querySongName,
                        onQuerySongChange = { viewModel.querySongName = it },
                        queryArtist = viewModel.queryArtistName,
                        onQueryArtistChange = { viewModel.queryArtistName = it },
                        onGetLyricsRequest = { viewModel.loadSongInfo(context) }
                    )

                    QueryStatus.Pending -> Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }

                    is QueryStatus.Success -> SuccessContent(
                        result = queryState.song,
                        onTryAgain = {
                            viewModel.lrcOffset = 0
                            viewModel.loadSongInfo(context, true)
                        },
                        onEdit = {
                            viewModel.lrcOffset = 0
                            viewModel.queryState = QueryStatus.NotSubmitted
                        },
                        directOffset = viewModel.userSettingsController.directlyModifyTimestamps,
                        offset = viewModel.lrcOffset,
                        onSetOffset = { viewModel.lrcOffset = it },
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
                            viewModel.embedLyrics(
                                it,
                                viewModel.source?.filePath,
                                context,
                                queryState.song
                            )
                        },
                        onCopyLyrics = {
                            clipboardManager.setText(AnnotatedString(it))

                            showToast(
                                context,
                                context.getString(R.string.lyrics_copied_to_clipboard),
                            )
                        },
                        openUri = uriHandler::openUri,
                        lyricsFetchState = viewModel.lyricsFetchState,
                        animatedVisibilityScope = animatedVisibilityScope,
                        disableMarquee = viewModel.userSettingsController.disableMarquee,
                        allowTryingAgain =
                            viewModel.userSettingsController.selectedProvider != Providers.APPLE &&
                            viewModel.userSettingsController.selectedProvider != Providers.MUSIXMATCH
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