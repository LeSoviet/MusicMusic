package com.musicmusic.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Control de volumen con slider vertical u horizontal.
 * 
 * @param volume Volumen actual (0.0 a 1.0)
 * @param onVolumeChange Callback cuando cambia el volumen
 * @param modifier Modificador de Compose
 * @param orientation Orientación del slider (vertical u horizontal)
 */
@Composable
fun VolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    orientation: VolumeOrientation = VolumeOrientation.Horizontal
) {
    var localVolume by remember { mutableStateOf(volume) }
    
    LaunchedEffect(volume) {
        localVolume = volume
    }
    
    when (orientation) {
        VolumeOrientation.Horizontal -> {
            HorizontalVolumeControl(
                volume = localVolume,
                onVolumeChange = {
                    localVolume = it
                    onVolumeChange(it)
                },
                modifier = modifier
            )
        }
        VolumeOrientation.Vertical -> {
            VerticalVolumeControl(
                volume = localVolume,
                onVolumeChange = {
                    localVolume = it
                    onVolumeChange(it)
                },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HorizontalVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ícono de volumen dinámico
        VolumeIcon(volume = volume)
        
        // Slider horizontal
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.width(120.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        
        // Porcentaje
        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Porcentaje arriba
        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Slider vertical
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.height(120.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        
        // Ícono de volumen abajo
        VolumeIcon(volume = volume)
    }
}

/**
 * Ícono de volumen que cambia según el nivel.
 */
@Composable
private fun VolumeIcon(
    volume: Float,
    modifier: Modifier = Modifier
) {
    val icon = when {
        volume == 0f -> Icons.Rounded.VolumeOff
        volume < 0.3f -> Icons.Rounded.VolumeMute
        volume < 0.7f -> Icons.Rounded.VolumeDown
        else -> Icons.Rounded.VolumeUp
    }
    
    Icon(
        imageVector = icon,
        contentDescription = "Volume",
        modifier = modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Botón de volumen con popup.
 */
@Composable
fun VolumeButton(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showVolumePopup by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        IconButton(
            onClick = { showVolumePopup = !showVolumePopup }
        ) {
            val icon = when {
                volume == 0f -> Icons.Rounded.VolumeOff
                volume < 0.3f -> Icons.Rounded.VolumeMute
                volume < 0.7f -> Icons.Rounded.VolumeDown
                else -> Icons.Rounded.VolumeUp
            }
            
            Icon(
                imageVector = icon,
                contentDescription = "Volume"
            )
        }
        
        // Popup con slider vertical
        AnimatedVisibility(
            visible = showVolumePopup,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it }
        ) {
            Box(
                modifier = Modifier
                    .offset(y = (-140).dp)
                    .width(60.dp)
            ) {
                VolumeControl(
                    volume = volume,
                    onVolumeChange = onVolumeChange,
                    orientation = VolumeOrientation.Vertical
                )
            }
        }
    }
}

enum class VolumeOrientation {
    Horizontal,
    Vertical
}
