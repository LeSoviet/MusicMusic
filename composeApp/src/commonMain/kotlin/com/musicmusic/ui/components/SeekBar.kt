package com.musicmusic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Barra de progreso interactiva para reproducción de audio.
 * 
 * Características:
 * - Arrastre para buscar posición
 * - Click para saltar a posición
 * - Indicador visual del progreso
 * - Hover effect premium
 * 
 * @param progress Progreso actual (0.0 a 1.0)
 * @param onSeekStart Callback cuando inicia el seek
 * @param onSeekChange Callback durante el arrastre
 * @param onSeekEnd Callback cuando termina el seek
 * @param modifier Modificador de Compose
 * @param enabled Si la barra está habilitada
 * @param progressColor Color de la barra de progreso
 * @param trackColor Color de la pista de fondo
 * @param thumbColor Color del indicador (thumb)
 */
@Composable
fun SeekBar(
    progress: Float,
    onSeekStart: (Float) -> Unit = {},
    onSeekChange: (Float) -> Unit = {},
    onSeekEnd: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = MaterialTheme.colorScheme.primary
) {
    var isDragging by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(progress) }
    
    // Sincronizar con el progreso externo cuando no se está arrastrando
    LaunchedEffect(progress) {
        if (!isDragging) {
            dragProgress = progress
        }
    }
    
    val currentProgress = if (isDragging) dragProgress else progress
    val barHeight = if (isDragging || isHovered) 6.dp else 4.dp
    val thumbRadius = if (isDragging || isHovered) 8.dp else 6.dp
    
    Box(
        modifier = modifier
            .height(32.dp)
            .fillMaxWidth()
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeekStart(newProgress)
                        onSeekChange(newProgress)
                        onSeekEnd()
                    }
                }
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            dragProgress = newProgress
                            onSeekStart(newProgress)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeekEnd()
                        },
                        onDragCancel = {
                            isDragging = false
                            onSeekEnd()
                        }
                    ) { change, _ ->
                        change.consume()
                        val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newProgress
                        onSeekChange(newProgress)
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2
            val barHeightPx = barHeight.toPx()
            val thumbRadiusPx = thumbRadius.toPx()
            
            // Barra de fondo (track)
            drawLine(
                color = trackColor,
                start = Offset(0f, centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = barHeightPx,
                cap = StrokeCap.Round
            )
            
            // Barra de progreso
            val progressX = canvasWidth * currentProgress
            if (progressX > 0) {
                drawLine(
                    color = progressColor,
                    start = Offset(0f, centerY),
                    end = Offset(progressX, centerY),
                    strokeWidth = barHeightPx,
                    cap = StrokeCap.Round
                )
            }
            
            // Indicador (thumb) - solo visible si está habilitado
            if (enabled && (isDragging || isHovered)) {
                drawCircle(
                    color = thumbColor,
                    radius = thumbRadiusPx,
                    center = Offset(progressX, centerY)
                )
                
                // Sombra del thumb
                drawCircle(
                    color = thumbColor.copy(alpha = 0.2f),
                    radius = thumbRadiusPx * 1.5f,
                    center = Offset(progressX, centerY)
                )
            }
        }
    }
}
