package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.components.SongCard
import pl.lambada.songsync.ui.screens.lyricsFetch.LyricsFetchState
import pl.lambada.songsync.util.Providers
import pl.lambada.songsync.util.applyOffsetToLyrics


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.SuccessContent(
    result: SongInfo,
    onTryAgain: () -> Unit,
    onEdit: () -> Unit,
    directOffset: Boolean,
    offset: Int,
    onSetOffset: (Int) -> Unit,
    onSaveLyrics: (String) -> Unit,
    onEmbedLyrics: (String) -> Unit,
    onCopyLyrics: (String) -> Unit,
    onLanguageSelected: (Long?, String) -> Unit = { _, _ -> },
    openUri: (String) -> Unit,
    lyricsFetchState: LyricsFetchState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    disableMarquee: Boolean,
    allowTryingAgain: Boolean,
    selectedProvider: Providers,
    onExpandProvidersRequest: () -> Unit,
) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Spacer(modifier = Modifier.height(10.dp))
    CloudProviderTitle(
        selectedProvider = selectedProvider,
        onExpandProvidersRequest = onExpandProvidersRequest,
    )
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

    Crossfade(lyricsFetchState, label = "") {
        Box(
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (it) {
                LyricsFetchState.NotSubmitted -> { /* nothing */ }

                is LyricsFetchState.Success -> LyricsSuccessContent(
                    lyrics = it.lyrics.let {
                        if (offset != 0 && directOffset) {
                            applyOffsetToLyrics(it, offset)
                        } else {
                            it
                        }
                    },
                    offset = offset,
                    onSetOffset = onSetOffset,
                    onSaveLyrics = { onSaveLyrics(it.lyrics) },
                    onEmbedLyrics = { onEmbedLyrics(it.lyrics) },
                    onCopyLyrics = { onCopyLyrics(it.lyrics) },
                    onLanguageSelected = { language ->
                        onLanguageSelected(result.musixmatchID, language)
                    },
                    availableLanguages = result.availableLanguages,
                    currentLanguage = result.currentLanguage,
                    originalLanguage = result.originalLanguage,
                )

                is LyricsFetchState.Failed -> Text(stringResource(R.string.this_track_has_no_lyrics))

                LyricsFetchState.Pending -> CircularProgressIndicator()
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}
