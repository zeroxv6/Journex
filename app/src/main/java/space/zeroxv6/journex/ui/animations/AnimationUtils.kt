package space.zeroxv6.journex.ui.animations
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
fun Modifier.bounceClick(
    enabled: Boolean = true,
    onClick: () -> Unit
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )
    this
        .scale(scale)
        .pointerInput(enabled) {
            if (enabled) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
        }
}
fun Modifier.shimmerEffect(): Modifier = composed {
    var offsetX by remember { mutableStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    this.graphicsLayer {
        offsetX = shimmerOffset
    }
}
fun slideInFromBottom(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeIn(animationSpec = tween(300))
}
fun slideOutToBottom(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeOut(animationSpec = tween(300))
}
fun fadeAndScaleIn(): EnterTransition {
    return fadeIn(animationSpec = tween(300)) +
            scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
}
fun fadeAndScaleOut(): ExitTransition {
    return fadeOut(animationSpec = tween(200)) +
            scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(200)
            )
}
fun expandVertically(): EnterTransition {
    return expandVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeIn()
}
fun shrinkVertically(): ExitTransition {
    return shrinkVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeOut()
}
fun slideInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeIn(animationSpec = tween(300))
}
fun slideOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it / 2 },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
}
@Composable
fun rememberPulseAnimation(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    return scale
}
@Composable
fun rememberShakeAnimation(trigger: Boolean): Float {
    var offset by remember { mutableStateOf(0f) }
    LaunchedEffect(trigger) {
        if (trigger) {
            val shakeKeyframes = keyframes<Float> {
                durationMillis = 400
                0f at 0
                -10f at 50
                10f at 100
                -10f at 150
                10f at 200
                -5f at 250
                5f at 300
                0f at 400
            }
            animate(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = shakeKeyframes
            ) { value, _ ->
                offset = value
            }
        }
    }
    return offset
}
@Composable
fun rememberRotationAnimation(isRotating: Boolean): Float {
    val rotation by animateFloatAsState(
        targetValue = if (isRotating) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    return rotation
}
fun staggeredFadeIn(index: Int, delayPerItem: Int = 50): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = index * delayPerItem,
            easing = FastOutSlowInEasing
        )
    ) + slideInVertically(
        initialOffsetY = { it / 4 },
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = index * delayPerItem,
            easing = FastOutSlowInEasing
        )
    )
}
@Composable
fun rememberSuccessAnimation(trigger: Boolean): Float {
    val scale by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "successScale"
    )
    return scale
}
