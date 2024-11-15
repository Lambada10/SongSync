package pl.lambada.songsync.activities.quicksearch

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import kotlinx.coroutines.Dispatchers
import pl.lambada.songsync.activities.quicksearch.viewmodel.QuickLyricsSearchViewModel
import pl.lambada.songsync.activities.quicksearch.viewmodel.QuickLyricsSearchViewModelFactory
import pl.lambada.songsync.data.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService
import pl.lambada.songsync.ui.theme.SongSyncTheme
import pl.lambada.songsync.util.dataStore

class QuickLyricsSearchActivity : AppCompatActivity() {

    val userSettingsController = UserSettingsController(dataStore)
    private val lyricsProviderService = LyricsProviderService()

    private val viewModel: QuickLyricsSearchViewModel by viewModels {
        QuickLyricsSearchViewModelFactory(userSettingsController, lyricsProviderService)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityImageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.35)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(7 * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .allowHardware(true)
            .crossfade(true)
            .bitmapFactoryMaxParallelism(12)
            .dispatcher(Dispatchers.IO)
            .build()


        enableEdgeToEdge()
        handleShareIntent(intent)

        setContent {
            val sheetState = rememberModalBottomSheetState()
            val viewModelState = viewModel.state.collectAsStateWithLifecycle()
            SongSyncTheme(pureBlack = userSettingsController.pureBlack) {
                ModalBottomSheet(
                    sheetState = sheetState,
                    properties = ModalBottomSheetDefaults.properties,
                    onDismissRequest = { this.finish() }
                ) {
                    QuickLyricsSearchPage(
                        state = viewModelState,
                        onSendLyrics = { lyrics ->
                            val resultIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra("lyrics", lyrics)
                                type = "text/plain"
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }


    private fun handleShareIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val songName =
                    intent.getStringExtra("songName") ?: "" //TODO: Change to a exception in the VM
                val artistName = intent.getStringExtra("artistName")
                    ?: "" //TODO: Change to a exception in the VM

                viewModel.onEvent(
                    QuickLyricsSearchViewModel.Event.Fetch(
                        songName to artistName,
                        this
                    )
                )
            }
        }
    }

    companion object {
        lateinit var activityImageLoader: ImageLoader
    }
}