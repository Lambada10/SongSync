package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.ProvidersDropdownMenuContent
import pl.lambada.songsync.ui.components.dropdown.AnimatedDropdownMenu
import pl.lambada.songsync.util.Providers
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_EXIT
import pl.lambada.songsync.util.ui.tweenEnter
import pl.lambada.songsync.util.ui.tweenExit

@Composable
fun HomeTopAppBarDropDown(
    onNavigateToSettingsSectionRequest: () -> Unit,
    selectedProvider: Providers,
    onProviderSelectRequest: (Providers) -> Unit,
    onBatchDownloadRequest: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var expandedProviders by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun dismissAll() {
        expanded = false
        scope.launch {
            delay(DURATION_EXIT.toLong())
            expandedProviders = false
        }
    }
    IconButton(onClick = { expanded = !expanded }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More"
        )
    }
    AnimatedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { dismissAll() }
    ) {
        AnimatedContent(
            targetState = expandedProviders,
            label = "",
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tweenEnter()
                    ),
                    initialContentExit = slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tweenExit()
                    ),
                )
            },
        ) {
            if(it)
                ProvidersDropdownMenuContent(
                    onDismissRequest = { dismissAll() },
                    selectedProvider = selectedProvider,
                    onProviderSelectRequest = onProviderSelectRequest
                )
            else
                HomeDropdownContent(
                    onExpandProviders = { expandedProviders = true },
                    onBatchDownloadRequest = {
                        expanded = false
                        onBatchDownloadRequest()
                    },
                    onNavigateToSettingsSectionRequest = {
                        expanded = false
                        onNavigateToSettingsSectionRequest()
                    },
                )
        }
    }
}

@Composable
fun HomeDropdownContent(
    onExpandProviders: () -> Unit,
    onBatchDownloadRequest: () -> Unit,
    onNavigateToSettingsSectionRequest: () -> Unit,
) = Column {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.provider),
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = "expand dropdown"
            )
        },
        onClick = onExpandProviders
    )
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.batch_download_lyrics),
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        },
        onClick = onBatchDownloadRequest
    )
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(id = R.string.settings),
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        },
        onClick = onNavigateToSettingsSectionRequest
    )
}