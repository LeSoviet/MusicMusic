package com.musicmusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState

/**
 * Custom title bar for the application window.
 * Replaces the native Windows title bar with a minimal, custom design.
 */
@Composable
fun WindowScope.CustomTitleBar(
    title: String,
    windowState: WindowState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    WindowDraggableArea {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(40.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Window controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize button
                    WindowButton(
                        onClick = { windowState.isMinimized = true },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    )

                    // Maximize/Restore button
                    WindowButton(
                        onClick = {
                            windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
                                WindowPlacement.Floating
                            } else {
                                WindowPlacement.Maximized
                            }
                        },
                        icon = {
                            // Simple square outline for maximize
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    )

                    // Close button (with red hover)
                    CloseWindowButton(onClick = onClose)
                }
            }
        }
    }
}

/**
 * Generic window control button (minimize, maximize).
 */
@Composable
private fun WindowButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .hoverable(interactionSource),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isHovered) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                Color.Transparent
            }
        )
    ) {
        icon()
    }
}

/**
 * Close button with red hover effect.
 */
@Composable
private fun CloseWindowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .hoverable(interactionSource),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isHovered) {
                Color(0xFFE81123) // Windows red
            } else {
                Color.Transparent
            }
        )
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = if (isHovered) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp)
        )
    }
}
