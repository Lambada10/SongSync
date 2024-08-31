package pl.lambada.songsync.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
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
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Navigator(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = ScreenHome
        ) {
            composable<ScreenHome> {
                HomeScreen(
                    navController = navController,
                    viewModel = viewModel,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                )
            }
            composable<ScreenSearch> {
                val args = it.toRoute<ScreenSearch>()
                SearchScreen(
                    id = args.id,
                    songName = args.songName,
                    artists = args.artists,
                    coverUri = args.coverUri,
                    filePath = args.filePath,
                    viewModel = viewModel,
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
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
}

@Serializable
object ScreenHome

@Serializable
data class ScreenSearch(
    val id: String? = null,
    val songName: String? = null,
    val artists: String? = null,
    val coverUri: String? = null,
    val filePath: String? = null,
)

@Serializable
object ScreenAbout