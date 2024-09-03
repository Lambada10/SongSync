package pl.lambada.songsync.data.remote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import pl.lambada.songsync.util.Providers
import pl.lambada.songsync.util.get
import pl.lambada.songsync.util.set

class UserSettingsController(private val dataStore: DataStore<Preferences>) {
    var embedLyricsIntoFiles by mutableStateOf(dataStore.get(embedKey, false))
        private set

    var selectedProvider by mutableStateOf(
        Providers.entries
            .find { it.displayName == dataStore.get(selectedProviderKey, Providers.SPOTIFY.displayName) }!!
    )
        private set

    var blacklistedFolders by mutableStateOf(
        dataStore.get(blacklistedFoldersKey, "").split(",")
    )
        private set
    var hideLyrics by mutableStateOf(false)
        private set

    var includeTranslation by mutableStateOf(
        dataStore.get(includeTranslationKey, false)
    )

    var pureBlack by mutableStateOf(false)
        private set

    var disableMarquee by mutableStateOf(dataStore.get(disableMarqueeKey, false))
        private set

    var sdCardPath by mutableStateOf(dataStore.get(sdCardPathKey, null))
        private set

    fun updateEmbedLyrics(to: Boolean) {
        dataStore.set(embedKey, to)
        embedLyricsIntoFiles = to
    }

    fun updateSelectedProviders(to: Providers) {
        dataStore.set(selectedProviderKey, to.displayName)
        selectedProvider = to
    }

    fun updateBlacklistedFolders(to: List<String>) {
        dataStore.set(blacklistedFoldersKey, to.joinToString(","))
        blacklistedFolders = to
    }

    fun updateHideLyrics(to: Boolean) {
        dataStore.set(hideLyricsKey, to)
        hideLyrics = to
    }

    fun updateIncludeTranslation(to: Boolean) {
        dataStore.set(includeTranslationKey, to)
        includeTranslation = to
    }

    fun updateDisableMarquee(to: Boolean) {
        dataStore.set(disableMarqueeKey, to)
        disableMarquee = to
    }

    fun updatePureBlack(to: Boolean) {
        dataStore.set(pureBlackKey, to)
        pureBlack = to
    }

    fun updateSdCardPath(to: String) {
        dataStore.set(sdCardPathKey, to)
        sdCardPath = to
    }
}

private val embedKey = booleanPreferencesKey("embed_lyrics")
private val selectedProviderKey = stringPreferencesKey("provider")
private val blacklistedFoldersKey = stringPreferencesKey("blacklist")
private val hideLyricsKey = booleanPreferencesKey("hide_lyrics")
private val includeTranslationKey = booleanPreferencesKey("include_translation")
private val disableMarqueeKey = booleanPreferencesKey("marquee_disable")
private val pureBlackKey = booleanPreferencesKey("pure_black")
private val sdCardPathKey = stringPreferencesKey("sd_card_path")