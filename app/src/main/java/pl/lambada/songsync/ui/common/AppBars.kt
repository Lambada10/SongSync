package pl.lambada.songsync.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import pl.lambada.songsync.data.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    CenterAlignedTopAppBar(
        title = { currentRoute?.let { Text(text = it) } }
    )
}

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar {
        val screens = Screens.values()
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.name,
                onClick = { navController.navigate(screen.name) },
                icon = {
                    Icon(
                        imageVector =
                            when(screen) {
                                Screens.Home -> if(currentRoute == screen.name) Icons.Filled.Home else Icons.Outlined.Home
                                Screens.Browse -> if(currentRoute == screen.name) Icons.Filled.Search else Icons.Outlined.Search
                                Screens.Settings -> if(currentRoute == screen.name) Icons.Filled.Settings else Icons.Outlined.Settings
                          },
                        contentDescription = screen.name)
                },
                label = { Text(text = screen.name) }
            )
        }
    }
}