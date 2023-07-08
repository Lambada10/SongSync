package pl.lambada.songsync.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
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
fun Navigator(navController: NavHostController, viewModel: MainViewModel) {
    NavHost(navController = navController, startDestination = Screens.Home.name) {
        composable(Screens.Home.name) {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screens.Browse.name) {
            BrowseScreen(viewModel = viewModel)
        }
        composable(Screens.About.name) {
            AboutScreen()
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