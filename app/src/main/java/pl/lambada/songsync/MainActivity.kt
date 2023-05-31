package pl.lambada.songsync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.Screens
import pl.lambada.songsync.ui.Navigator
import pl.lambada.songsync.ui.common.BottomBar
import pl.lambada.songsync.ui.common.TopBar
import pl.lambada.songsync.ui.theme.SongSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = MainViewModel()
            val navController = rememberNavController()

            // Get token upon app start
            Thread {
                viewModel.refreshToken()
            }.start()

            SongSyncTheme {
                Scaffold(
                    topBar = {
                        TopBar(navController = navController) },
                    bottomBar = {
                        BottomBar(navController = navController) }
                ) {paddingValues ->
                    Surface(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)) {
                        Navigator(navController = navController, viewModel = viewModel)
                    }
                }
            }
        }
    }
}