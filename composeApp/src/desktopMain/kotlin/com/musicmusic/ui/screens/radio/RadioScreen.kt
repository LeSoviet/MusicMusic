package com.musicmusic.ui.screens.radio

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.model.Radio
import com.musicmusic.ui.theme.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * Pantalla principal de radios online.
 */
@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val radios by viewModel.radios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val availableGenres by viewModel.availableGenres.collectAsState()
    val availableCountries by viewModel.availableCountries.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            RadioTopBar(
                searchQuery = viewModel.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                showOnlyFavorites = showOnlyFavorites,
                onToggleFavorites = viewModel::toggleShowFavorites,
                onToggleFilters = { showFilters = !showFilters }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filtros
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    RadioFilters(
                        selectedGenre = selectedGenre,
                        selectedCountry = selectedCountry,
                        availableGenres = availableGenres,
                        availableCountries = availableCountries,
                        onGenreSelected = viewModel::selectGenre,
                        onCountrySelected = viewModel::selectCountry,
                        onClearFilters = viewModel::clearFilters
                    )
                }
                
                // Grid de radios
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (radios.isEmpty()) {
                    EmptyRadioState(
                        showOnlyFavorites = showOnlyFavorites,
                        hasFilters = selectedGenre != null || selectedCountry != null || viewModel.searchQuery.isNotBlank()
                    )
                } else {
                    RadioGrid(
                        radios = radios,
                        onRadioClick = viewModel::playRadio,
                        onToggleFavorite = viewModel::toggleFavorite
                    )
                }
            }
        }
    }
}

/**
 * Barra superior con búsqueda y controles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadioTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit,
    onToggleFilters: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título
            Text(
                text = "📻 Radios Online",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar radios...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Botón de favoritos
                IconButton(
                    onClick = onToggleFavorites,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (showOnlyFavorites) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (showOnlyFavorites) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritos",
                        tint = if (showOnlyFavorites) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Botón de filtros
                IconButton(
                    onClick = onToggleFilters,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Panel de filtros.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadioFilters(
    selectedGenre: String?,
    selectedCountry: String?,
    availableGenres: List<String>,
    availableCountries: List<String>,
    onGenreSelected: (String?) -> Unit,
    onCountrySelected: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onClearFilters) {
                    Text("Limpiar filtros")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Filtro de género
            Text(
                text = "Género",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableGenres.forEach { genre ->
                    FilterChip(
                        selected = selectedGenre == genre,
                        onClick = {
                            onGenreSelected(if (selectedGenre == genre) null else genre)
                        },
                        label = { Text(genre) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Filtro de país
            Text(
                text = "País",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCountries.take(10).forEach { country ->
                    FilterChip(
                        selected = selectedCountry == country,
                        onClick = {
                            onCountrySelected(if (selectedCountry == country) null else country)
                        },
                        label = { Text(country) }
                    )
                }
            }
        }
    }
}

/**
 * Grid de radios.
 */
@Composable
private fun RadioGrid(
    radios: List<Radio>,
    onRadioClick: (Radio) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(radios, key = { it.id }) { radio ->
            RadioCard(
                radio = radio,
                onClick = { onRadioClick(radio) },
                onToggleFavorite = { onToggleFavorite(radio.id) }
            )
        }
    }
}

/**
 * Card de radio individual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadioCard(
    radio: Radio,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Nombre
                    Text(
                        text = radio.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Género
                    if (radio.genre != null) {
                        Text(
                            text = radio.genre,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // País
                    if (radio.country != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = radio.country,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Bitrate
                    if (radio.bitrate != null) {
                        Text(
                            text = "${radio.bitrate}kbps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Botón de favorito
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (radio.isFavorite) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (radio.isFavorite) Color(0xFFE91E63)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Estado vacío.
 */
@Composable
private fun EmptyRadioState(
    showOnlyFavorites: Boolean,
    hasFilters: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Text(
                text = when {
                    showOnlyFavorites -> "No tienes radios favoritas"
                    hasFilters -> "No se encontraron radios"
                    else -> "No hay radios disponibles"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (hasFilters) {
                Text(
                    text = "Intenta cambiar los filtros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * FlowRow para chips (no disponible en Compose Desktop, implementación simple).
 */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    // Implementación simple de FlowRow
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}
