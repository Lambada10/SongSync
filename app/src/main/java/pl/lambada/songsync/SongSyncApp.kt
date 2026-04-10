package pl.lambada.songsync
import android.app.Application
import pl.lambada.songsync.data.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService
import pl.lambada.songsync.util.dataStore
class SongSyncApp : Application() {
    lateinit var userSettingsController: UserSettingsController
        private set
    
    val lyricsProviderService = LyricsProviderService()
    override fun onCreate() {
        super.onCreate()
        userSettingsController = UserSettingsController(dataStore)
    }
}
