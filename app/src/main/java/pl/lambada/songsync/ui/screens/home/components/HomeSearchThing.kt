package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeSearchThing(
    showingSearch: Boolean,
    searchBar: @Composable () -> Unit,
    filterBar: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = showingSearch,
        transitionSpec = {
            if (targetState) {
                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height } + fadeOut()
                )
            } else {
                (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> height } + fadeOut()
                )
            }.using(
                SizeTransform()
            )
        },
        label = "",
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
    ) { showing ->
        if (showing) searchBar() else filterBar()
    }
}

