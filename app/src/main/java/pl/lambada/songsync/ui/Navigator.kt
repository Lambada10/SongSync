package pl.lambada.songsync.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.serialization.Serializable
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.ui.screens.AboutScreen
import pl.lambada.songsync.ui.screens.SearchScreen
import pl.lambada.songsync.ui.screens.HomeScreen

/**
 * Composable function for handling navigation within the app.
 *
 * @param navController The navigation controller.
 * @param viewModel The main view model.
 */
@Composable
fun Navigator(
    navController: NavHostController,
    selected: SnapshotStateList<String>,
    allSongs: List<Song>?,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = ScreenHome
    ) {
        composable<ScreenHome> {
            HomeScreen(
                navController = navController,
                selected = selected,
                allSongs = allSongs,
                viewModel = viewModel,
            )
        }
        composable<ScreenSearch> {
            SearchScreen(
                viewModel = viewModel,
                navController = navController,
            )
        }
        composable<ScreenAbout> {
            AboutScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

@Serializable
object ScreenHome

@Serializable
object ScreenSearch

@Serializable
object ScreenAbout