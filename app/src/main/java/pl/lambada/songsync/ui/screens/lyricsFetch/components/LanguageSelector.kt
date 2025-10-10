package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.data.remote.lyrics_providers.others.MusixmatchAPI

@Composable
fun LanguageSelector(
    availableLanguages: List<String>,
    currentLanguage: String?,
    originalLanguage: String?,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    musixmatchAPI: MusixmatchAPI = MusixmatchAPI()
) {
    var expanded by remember { mutableStateOf(false) }

    if (availableLanguages.isEmpty()) return

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentLanguage?.let {
                        musixmatchAPI.getLanguageDisplayName(it)
                    } ?: stringResource(R.string.select_language)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            // Original language first
            originalLanguage?.let { origLang ->
                DropdownMenuItem(
                    text = {
                        Text("${musixmatchAPI.getLanguageDisplayName(origLang)} (Original)")
                    },
                    onClick = {
                        onLanguageSelected(origLang)
                        expanded = false
                    }
                )
            }

            // Other available languages
            availableLanguages.filter { it != originalLanguage }.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Text(musixmatchAPI.getLanguageDisplayName(language))
                    },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}