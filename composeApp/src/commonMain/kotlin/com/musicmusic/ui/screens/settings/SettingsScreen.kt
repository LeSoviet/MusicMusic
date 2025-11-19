package com.musicmusic.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicmusic.ui.keyboard.KeyboardShortcuts
import com.musicmusic.ui.theme.ThemeManager
import org.koin.compose.koinInject

/**
 * Pantalla de configuración de la aplicación
 * 
 * Incluye:
 * - Configuración de tema
 * - Carpeta de música
 * - Configuración de audio
 * - Atajos de teclado (info)
 * - Acerca de
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeManager = koinInject<ThemeManager>()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    var showKeyboardShortcuts by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Apariencia
            SettingsSection(title = "Apariencia") {
                SettingsItem(
                    icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Modo Oscuro",
                    description = "Cambia entre tema claro y oscuro"
                ) {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { themeManager.toggleTheme() }
                    )
                }
            }
            
            // Audio
            SettingsSection(title = "Audio") {
                SettingsItem(
                    icon = Icons.Default.MusicNote,
                    title = "Calidad de Audio",
                    description = "Máxima calidad disponible"
                ) {
                    Text(
                        text = "Alta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingsItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Normalización de Volumen",
                    description = "Ajusta automáticamente el volumen entre canciones"
                ) {
                    Switch(
                        checked = false,
                        onCheckedChange = { /* TODO */ }
                    )
                }
            }
            
            // Biblioteca
            SettingsSection(title = "Biblioteca") {
                SettingsItem(
                    icon = Icons.Default.Folder,
                    title = "Carpeta de Música",
                    description = "Cambiar ubicación de la biblioteca",
                    onClick = { /* TODO: Abrir selector de carpeta */ }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "Actualizar Biblioteca",
                    description = "Escanear nuevos archivos",
                    onClick = { /* TODO: Iniciar escaneo */ }
                )
            }
            
            // Atajos de teclado
            SettingsSection(title = "Atajos de Teclado") {
                SettingsItem(
                    icon = Icons.Default.Keyboard,
                    title = "Ver Atajos",
                    description = "Lista de todos los atajos disponibles",
                    onClick = { showKeyboardShortcuts = true }
                )
            }
            
            // Acerca de
            SettingsSection(title = "Acerca de") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "MusicMusic",
                    description = "Versión 1.0.0 - Fase 6"
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Desarrollado con",
                    description = "Kotlin Multiplatform + Compose Desktop"
                )
            }
        }
    }
    
    // Dialog de atajos de teclado
    if (showKeyboardShortcuts) {
        KeyboardShortcutsDialog(
            onDismiss = { showKeyboardShortcuts = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
private fun KeyboardShortcutsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Atajos de Teclado")
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KeyboardShortcuts.getShortcutDescription().forEach { (shortcut, description) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = shortcut,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
