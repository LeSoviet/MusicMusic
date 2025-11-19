package com.musicmusic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.error.AppError
import com.musicmusic.domain.error.ErrorSeverity
import kotlinx.coroutines.delay

/**
 * Snackbar para mostrar errores de la aplicación.
 * 
 * Se muestra en la parte inferior de la pantalla con diferentes colores
 * según la severidad del error.
 * 
 * @param error El error a mostrar, o null si no hay error
 * @param onDismiss Callback cuando el usuario cierra el error
 * @param modifier Modificador opcional
 */
@Composable
fun ErrorSnackbar(
    error: AppError?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    // Auto-dismiss después de 5 segundos para errores de baja severidad
    LaunchedEffect(error) {
        if (error != null) {
            visible = true
            
            // Auto-dismiss para errores de baja severidad
            if (error.getSeverity() == ErrorSeverity.LOW) {
                delay(5000)
                visible = false
                delay(300) // Esperar la animación
                onDismiss()
            }
        } else {
            visible = false
        }
    }
    
    AnimatedVisibility(
        visible = visible && error != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        error?.let {
            ErrorSnackbarContent(
                error = it,
                onDismiss = {
                    visible = false
                    onDismiss()
                }
            )
        }
    }
}

/**
 * Contenido del snackbar de error.
 */
@Composable
private fun ErrorSnackbarContent(
    error: AppError,
    onDismiss: () -> Unit
) {
    val (backgroundColor, contentColor, icon) = when (error.getSeverity()) {
        ErrorSeverity.HIGH -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Error
        )
        ErrorSeverity.MEDIUM -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.9f), // Orange
            Color.White,
            Icons.Default.Warning
        )
        ErrorSeverity.LOW -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Info
        )
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono de severidad
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            // Mensaje de error
            Text(
                text = error.getUserMessage(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            // Botón de cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
