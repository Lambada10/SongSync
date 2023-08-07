package pl.lambada.songsync.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val darkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val lightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Custom SongSync theme that applies the desired color scheme and system UI adjustments.
 *
 * @param darkTheme Whether to use dark theme based on system settings.
 * @param dynamicColor Whether to use dynamic color scheme (available on Android 12+).
 * @param content The content of the theme.
 */
@Composable
fun SongSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme)
                if (pureBlack)
                    dynamicDarkColorScheme(context).copy(
                        surface = Color.Black,
                        background = Color.Black,
                    )
                else
                    dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
        }
        pureBlack -> darkColorScheme.copy(
            surface = Color.Black,
            background = Color.Black,
        )
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }
    val sysUiController = rememberSystemUiController()
    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(Unit) {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            sysUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = !darkTheme,
                isNavigationBarContrastEnforced = false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
