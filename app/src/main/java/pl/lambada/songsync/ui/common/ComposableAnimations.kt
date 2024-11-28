package pl.lambada.songsync.ui.common

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import pl.lambada.songsync.util.ui.materialSharedAxisXIn
import pl.lambada.songsync.util.ui.materialSharedAxisXOut
import pl.lambada.songsync.util.ui.materialSharedAxisYIn
import pl.lambada.songsync.util.ui.materialSharedAxisYOut

val AnimatedTextContentTransformation = ContentTransform(
    materialSharedAxisXIn(initialOffsetX = { it / 10 }),
    materialSharedAxisXOut(targetOffsetX = { -it / 10 }),
    sizeTransform = SizeTransform(clip = false)
)

val AnimatedCardContentTransformation = ContentTransform(
    materialSharedAxisYIn(initialOffsetY = { it / 10 }),
    materialSharedAxisYOut(targetOffsetY = { -it / 10 }),
    sizeTransform = SizeTransform(clip = false)
)