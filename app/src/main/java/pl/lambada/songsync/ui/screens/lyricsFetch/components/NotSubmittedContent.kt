package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.CommonTextField


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
