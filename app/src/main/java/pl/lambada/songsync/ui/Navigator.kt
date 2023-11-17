package pl.lambada.songsync.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.dto.Song
import pl.lambada.songsync.ui.screens.AboutScreen
import pl.lambada.songsync.ui.screens.BrowseScreen
import pl.lambada.songsync.ui.screens.HomeScreen

/**
 * Composable function for handling navigation within the app.
 *
 * @param navController The navigation controller.
 * @param viewModel The main view model.
 */
@Composable
fun Navigator(
    navController: NavHostController, selected: SnapshotStateList<String>,
    allSongs: List<Song>?, viewModel: MainViewModel
) {
    NavHost(navController = navController, startDestination = Screens.Home.name) {
        composable(Screens.Home.name) {
            HomeScreen(
                navController = navController, selected = selected,
                allSongs = allSongs, viewModel = viewModel
            )
        }
        composable(Screens.Browse.name) {
            BrowseScreen(viewModel = viewModel)
        }
        composable(Screens.About.name) {
            AboutScreen(viewModel = viewModel)
        }
    }
}

/**
 * Enum class for navigation.
 */
enum class Screens(val stringResource: Int) {
    Home(R.string.home),
    Browse(R.string.browse),
    About(R.string.about)
}