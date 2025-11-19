package com.musicmusic.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Animaciones y transiciones personalizadas para MusicMusic
 */
object AppAnimations {
    
    /**
     * Duración estándar de las animaciones
     */
    const val DURATION_SHORT = 200
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 500
    
    /**
     * Easing curves personalizadas
     */
    val EaseInOutCubic = CubicBezierEasing(0.65f, 0.05f, 0.36f, 1f)
    val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
    val EaseInQuart = CubicBezierEasing(0.5f, 0f, 0.75f, 0f)
    
    /**
     * Spec de animación por defecto
     */
    fun <T> defaultSpring(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessLow
    ): SpringSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness
    )
    
    /**
     * Tween por defecto
     */
    fun <T> defaultTween(
        durationMillis: Int = DURATION_MEDIUM,
        easing: Easing = EaseInOutCubic
    ): TweenSpec<T> = tween(
        durationMillis = durationMillis,
        easing = easing
    )
    
    /**
     * Transición de fade suave
     */
    @Composable
    fun <T> AnimatedContent(
        targetState: T,
        modifier: Modifier = Modifier,
        transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
            fadeIn(animationSpec = tween(DURATION_MEDIUM, easing = EaseInOutCubic)) togetherWith
                    fadeOut(animationSpec = tween(DURATION_MEDIUM, easing = EaseInOutCubic))
        },
        content: @Composable AnimatedContentScope.(T) -> Unit
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState = targetState,
            modifier = modifier,
            transitionSpec = transitionSpec,
            content = content
        )
    }
    
    /**
     * Transición slide horizontal
     */
    fun <T> slideHorizontalTransition(): AnimatedContentTransitionScope<T>.() -> ContentTransform = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(DURATION_MEDIUM, easing = EaseOutQuart)
        ) + fadeIn(
            animationSpec = tween(DURATION_MEDIUM)
        ) togetherWith slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(DURATION_MEDIUM, easing = EaseInQuart)
        ) + fadeOut(
            animationSpec = tween(DURATION_MEDIUM)
        )
    }
    
    /**
     * Transición scale + fade
     */
    fun <T> scaleTransition(): AnimatedContentTransitionScope<T>.() -> ContentTransform = {
        fadeIn(
            animationSpec = tween(DURATION_MEDIUM)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(DURATION_MEDIUM, easing = EaseOutQuart)
        ) togetherWith fadeOut(
            animationSpec = tween(DURATION_SHORT)
        ) + scaleOut(
            targetScale = 1.08f,
            animationSpec = tween(DURATION_SHORT)
        )
    }
    
    /**
     * Animación de pulsación (para botones)
     */
    @Composable
    fun rememberPulseAnimation(): InfiniteTransition {
        return rememberInfiniteTransition(label = "pulse")
    }
}

/**
 * Extension para animar cambios de tamaño
 */
fun Modifier.animatedSize() = this.then(
    Modifier.animateContentSize(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
)
