package pl.lambada.songsync.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import pl.lambada.songsync.data.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = Screens.values()

    val currentScreen = screens.firstOrNull { it.name == currentRoute }.toString()

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = currentScreen,
                fontWeight = FontWeight.Bold
            )
        },
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
                onClick = {
                    if (currentRoute != screen.name) {
                        if (screen == Screens.Home) {
                            navController.popBackStack(navController.graph.startDestinationId, false)
                        } else {
                            navController.navigate(screen.name)
                        }
                    }
                },
                icon = {
                    val isSelected = currentRoute == screen.name
                    val imageVector = when (screen) {
                        Screens.Home -> if (isSelected) Icons.Default.Home else Icons.Outlined.Home
                        Screens.Browse -> if (isSelected) Icons.Default.Search else Icons.Outlined.Search
                        Screens.About -> if (isSelected) Icons.Default.Info else Icons.Outlined.Info
                    }
                    Icon(imageVector, contentDescription = screen.name)
                },
                label = { Text(text = screen.toString()) })
        }
    }
}