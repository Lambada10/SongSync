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

        // Disable the default system window insets handling
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set a listener to handle window insets manually
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            // Remove any padding from the view
            v.setPadding(0, 0, 0, 0)
            insets
        }

        // Configure the window properties
        window.run {
            // Set the background to be transparent
            setBackgroundDrawable(ColorDrawable(0))
            // Set the window layout to match the parent dimensions
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
            )
            // Set the window type based on the Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android O and above, use TYPE_APPLICATION_OVERLAY
                setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                // For older versions, use TYPE_SYSTEM_ALERT
                setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }

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