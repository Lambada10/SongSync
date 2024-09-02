package pl.lambada.songsync.ui.screens.lyricsFetch

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import pl.lambada.songsync.ui.screens.about.AboutViewModel
import pl.lambada.songsync.ui.screens.about.Providers
import pl.lambada.songsync.ui.screens.lyricsFetch.components.FailedDialogue
import pl.lambada.songsync.ui.screens.lyricsFetch.components.LocalSongContent
import pl.lambada.songsync.ui.screens.lyricsFetch.components.NoConnectionDialogue
import pl.lambada.songsync.ui.screens.lyricsFetch.components.NotSubmittedContent
import pl.lambada.songsync.ui.screens.lyricsFetch.components.SuccessContent

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