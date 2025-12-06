package com.musicmusic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Custom minimal scrollbar for LazyColumn/LazyRow.
 * Auto-hides when not in use, shows on hover.
 */
@Composable
fun CustomVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scope = rememberCoroutineScope()

    // Calculate scrollbar properties
    val layoutInfo = listState.layoutInfo
    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val totalItemsHeight = layoutInfo.totalItemsCount *
        (layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0)

    val scrollbarHeight = if (totalItemsHeight > 0) {
        (viewportHeight.toFloat() / totalItemsHeight.toFloat()).coerceIn(0.1f, 1f)
    } else {
        0f
    }

    val scrollProgress = if (layoutInfo.totalItemsCount > 0) {
        listState.firstVisibleItemIndex.toFloat() / layoutInfo.totalItemsCount.toFloat()
    } else {
        0f
    }

    // Auto-hide when not scrolling
    val isScrolling = listState.isScrollInProgress
    val targetAlpha = when {
        isHovered -> 1f
        isScrolling -> 0.7f
        else -> 0f
    }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
            .hoverable(interactionSource)
            .alpha(alpha)
    ) {
        // Scrollbar track (subtle background)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    RoundedCornerShape(4.dp)
                )
        )

        // Scrollbar thumb
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth()
                .fillMaxHeight(scrollbarHeight)
                .offset(y = (scrollProgress * viewportHeight).dp)
                .background(
                    if (isHovered) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                    RoundedCornerShape(4.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val dragPercentage = dragAmount.y / viewportHeight
                            val targetIndex = (listState.firstVisibleItemIndex +
                                (dragPercentage * layoutInfo.totalItemsCount).toInt())
                                .coerceIn(0, layoutInfo.totalItemsCount - 1)
                            listState.scrollToItem(targetIndex)
                        }
                    }
                }
        )
    }
}

/**
 * Wrapper that adds custom scrollbar to any scrollable content.
 */
@Composable
fun BoxWithCustomScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        content()

        // Custom scrollbar on the right
        CustomVerticalScrollbar(
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
        )
    }
}
