package pl.lambada.songsync.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.Screens
import pl.lambada.songsync.ui.screens.BrowseScreen
import pl.lambada.songsync.ui.screens.HomeScreen
import pl.lambada.songsync.ui.screens.SettingsScreen

@Composable
fun Navigator(navController: NavHostController, viewModel: MainViewModel) {
    NavHost(navController = navController, startDestination = Screens.Home.name) {
        composable(Screens.Home.name) {
            HomeScreen(viewModel = viewModel)
        }
        composable(Screens.Browse.name) {
            BrowseScreen(viewModel = viewModel)
        }
        composable(route = Screens.Settings.name) {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }
    }
}