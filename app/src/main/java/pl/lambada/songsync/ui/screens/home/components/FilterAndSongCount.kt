package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R

@OptIn(ExperimentalMaterial3Api::class)
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
                Icons.Outlined.FilterAlt,
                contentDescription = stringResource(R.string.search),
            )
        }

        IconButton(onClick = onSearchClick) {
            Icon(
                Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
            )
        }
    }
}