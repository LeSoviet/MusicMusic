package com.musicmusic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.model.EqualizerPreset
import com.musicmusic.ui.screens.player.PlayerViewModel
import org.koin.compose.koinInject

@Composable
fun EqualizerPanel(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = koinInject()
) {
    val equalizerSettings = playerViewModel.equalizerSettings
    val availablePresets = playerViewModel.availablePresets
    val equalizerBands = playerViewModel.equalizerBands
    val volumeNormalizerSettings = playerViewModel.volumeNormalizerSettings
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título del panel
            Text(
                text = "Controles de Audio",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Separador
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            // Sección de Ecualizador
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Encabezado del ecualizador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ecualizador",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Botón para activar/desactivar el ecualizador
                    Switch(
                        checked = equalizerSettings.isEnabled,
                        onCheckedChange = { playerViewModel.toggleEqualizer() }
                    )
                }
                
                // Controles del ecualizador (solo si está activo)
                if (equalizerSettings.isEnabled) {
                    // Selector de presets
                    PresetSelector(
                        presets = availablePresets,
                        onPresetSelected = { presetName ->
                            // Buscar el preset por nombre y aplicarlo
                            val preset = EqualizerPreset.getPresetByName(presetName)
                            preset?.let { playerViewModel.applyEqualizerPreset(it) }
                        }
                    )
                    
                    // Preamplificación con explicación
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Preamplificación",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "%.1f dB".format(equalizerSettings.preamp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = "Ajusta el nivel general de todas las bandas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        Slider(
                            value = equalizerSettings.preamp,
                            onValueChange = { playerViewModel.setEqualizerPreamp(it) },
                            valueRange = -20f..20f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Controles de bandas de frecuencia
                    Text(
                        text = "Bandas de Frecuencia",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Mostrar controles para cada banda
                    equalizerBands.forEachIndexed { index, frequency ->
                        // Solo mostrar si hay una banda correspondiente en la configuración
                        if (index < equalizerSettings.bands.size) {
                            val gain = equalizerSettings.bands[index]
                            val frequencyText = when {
                                frequency >= 1000 -> "${(frequency / 1000).toInt()}k"
                                else -> "${frequency.toInt()}"
                            }
                            
                            LabeledSlider(
                                label = frequencyText,
                                value = gain,
                                valueRange = -20f..20f,
                                onValueChange = { playerViewModel.setEqualizerBand(index, it) },
                                valueFormatter = { "%.1f dB".format(it) }
                            )
                        }
                    }
                }
            }
            
            // Separador
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            // Sección de Normalización de Volumen
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Encabezado del normalizador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Normalización de Volumen",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Switch(
                        checked = volumeNormalizerSettings.isEnabled,
                        onCheckedChange = { playerViewModel.toggleVolumeNormalizer() }
                    )
                }
                
                // Control de nivel de normalización (solo si está activo)
                if (volumeNormalizerSettings.isEnabled) {
                    LabeledSlider(
                        label = "Nivel de Normalización",
                        value = volumeNormalizerSettings.level,
                        valueRange = 0f..1f,
                        onValueChange = { playerViewModel.setVolumeNormalizerLevel(it) },
                        valueFormatter = { "%.0f%%".format(it * 100) }
                    )
                    
                    Text(
                        text = "Nivela el volumen entre diferentes canciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Slider con etiqueta y valor formateado.
 */
@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueFormatter: (Float) -> String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = valueFormatter(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Selector de presets predefinidos.
 */
@Composable
private fun PresetSelector(
    presets: List<String>,
    onPresetSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf("Flat") }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Presets",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                value = selectedPreset,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                presets.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset) },
                        onClick = {
                            selectedPreset = preset
                            expanded = false
                            onPresetSelected(preset)
                        }
                    )
                }
            }
        }
    }
}