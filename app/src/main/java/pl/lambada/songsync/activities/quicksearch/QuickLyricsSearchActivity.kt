package pl.lambada.songsync.activities.quicksearch

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.lambada.songsync.activities.quicksearch.viewmodel.QuickLyricsSearchViewModel
import pl.lambada.songsync.activities.quicksearch.viewmodel.QuickLyricsSearchViewModelFactory
import pl.lambada.songsync.data.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService
import pl.lambada.songsync.ui.theme.SongSyncTheme
import pl.lambada.songsync.util.dataStore

class QuickLyricsSearchActivity : ComponentActivity() {

    val userSettingsController = UserSettingsController(dataStore)
    private val lyricsProviderService = LyricsProviderService()

    private val viewModel: QuickLyricsSearchViewModel by viewModels {
        QuickLyricsSearchViewModelFactory(userSettingsController, lyricsProviderService)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                val songName = intent.getStringExtra("songName") ?: "" //TODO: Change to a exception in the VM
                val artistName = intent.getStringExtra("artistName") ?: "" //TODO: Change to a exception in the VM

                viewModel.onEvent(QuickLyricsSearchViewModel.Event.Fetch(songName to artistName, this))
            }
        }
    }
}