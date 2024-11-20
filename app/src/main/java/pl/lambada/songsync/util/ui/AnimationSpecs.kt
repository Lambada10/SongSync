package pl.lambada.songsync.util.ui

import android.graphics.Path
import android.view.animation.PathInterpolator
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import pl.lambada.songsync.util.ui.MotionConstants.DURATION
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_ENTER
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_ENTER_SHORT
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_EXIT
import pl.lambada.songsync.util.ui.MotionConstants.DURATION_EXIT_SHORT

fun PathInterpolator.toEasing(): Easing {
    return Easing { f -> this.getInterpolation(f) }
}

private val path = Path().apply {
    moveTo(0f, 0f)
    cubicTo(0.05F, 0F, 0.133333F, 0.06F, 0.166666F, 0.4F)
    cubicTo(0.208333F, 0.82F, 0.25F, 1F, 1F, 1F)
}

val EmphasizedPathInterpolator = PathInterpolator(path)
val EmphasizedEasing = EmphasizedPathInterpolator.toEasing()

val EmphasizeEasingVariant = CubicBezierEasing(.2f, 0f, 0f, 1f)
val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)
val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

val StandardDecelerate = CubicBezierEasing(.0f, .0f, 0f, 1f)
val MotionEasingStandard = CubicBezierEasing(0.4F, 0.0F, 0.2F, 1F)

val tweenSpec = tween<Float>(durationMillis = DURATION_ENTER, easing = EmphasizedEasing)

fun <T> tweenEnter(
    delayMillis: Int = DURATION_EXIT,
    durationMillis: Int = DURATION_ENTER
) =
    tween<T>(
        delayMillis = delayMillis,
        durationMillis = durationMillis,
        easing = EmphasizedDecelerateEasing
    )

fun <T> tweenExit(
    durationMillis: Int = DURATION_EXIT_SHORT,
) = tween<T>(
    durationMillis = durationMillis,
    easing = EmphasizedAccelerateEasing
)

@OptIn(ExperimentalSharedTransitionApi::class)
val DefaultBoundsTransform = BoundsTransform { _, _ ->
    tween(easing = EmphasizedEasing, durationMillis = DURATION)
}

@OptIn(ExperimentalAnimationSpecApi::class, ExperimentalSharedTransitionApi::class)
val SearchFABBoundsTransform = BoundsTransform { initialBounds, targetBounds ->
    keyframes {
        durationMillis = DURATION_ENTER_SHORT
        initialBounds at 0 using ArcMode.ArcBelow using MotionEasingStandard
        targetBounds at DURATION_ENTER_SHORT using ArcMode.ArcAbove using MotionEasingStandard
    }
}

