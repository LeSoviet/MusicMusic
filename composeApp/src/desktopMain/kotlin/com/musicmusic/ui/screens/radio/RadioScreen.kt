package com.musicmusic.ui.screens.radio

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
 * Main online radio screen with genre categorization.
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

    Scaffold(
        modifier = modifier,
        topBar = {
            RadioTopBar(
                searchQuery = viewModel.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                showOnlyFavorites = showOnlyFavorites,
                onToggleFavorites = viewModel::toggleShowFavorites,
                selectedGenre = selectedGenre,
                availableGenres = availableGenres,
                onGenreSelected = viewModel::selectGenre
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Radio list
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
                RadioList(
                    radios = radios,
                    onRadioClick = viewModel::playRadio,
                    onToggleFavorite = viewModel::toggleFavorite,
                    selectedGenre = selectedGenre
                )
            }
        }
    }
}

/**
 * Top bar with search and genre filter controls.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RadioTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit,
    selectedGenre: String?,
    availableGenres: List<String>,
    onGenreSelected: (String?) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Title
            Text(
                text = "Radio Stations",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search radios...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "", modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                )

                // Favorites button
                IconButton(
                    onClick = onToggleFavorites,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (showOnlyFavorites) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorites",
                        tint = if (showOnlyFavorites) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Genre filters
            if (availableGenres.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // All genres chip
                    FilterChip(
                        selected = selectedGenre == null,
                        onClick = { onGenreSelected(null) },
                        label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(28.dp)
                    )

                    availableGenres.forEach { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = { onGenreSelected(if (selectedGenre == genre) null else genre) },
                            label = { Text(genre, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Panel de filtros.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
 * List of radios grouped by genre.
 */
@Composable
private fun RadioList(
    radios: List<Radio>,
    onRadioClick: (Radio) -> Unit,
    onToggleFavorite: (String) -> Unit,
    selectedGenre: String?
) {
    val radiosByGenre = if (selectedGenre != null) {
        mapOf(selectedGenre to radios)
    } else {
        radios.groupBy { it.genre ?: "Unknown" }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 80.dp  // Extra space for PlayerBar
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        radiosByGenre.forEach { (genre, genreRadios) ->
            // Genre header
            if (selectedGenre == null) {
                item(key = "header_$genre") {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Radios in genre
            items(
                items = genreRadios,
                key = { it.id }
            ) { radio ->
                RadioItem(
                    radio = radio,
                    onClick = { onRadioClick(radio) },
                    onToggleFavorite = { onToggleFavorite(radio.id) }
                )
            }
        }
    }
}

/**
 * Individual radio item (compact design).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadioItem(
    radio: Radio,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = if (isHovered) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        } else {
            Color.Transparent
        },
        border = if (isHovered) {
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .hoverable(interactionSource),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Radio icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Radio info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = radio.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country
                    if (radio.country != null) {
                        Text(
                            text = radio.country,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bitrate
                    if (radio.bitrate != null) {
                        if (radio.country != null) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${radio.bitrate}kbps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Favorite button
            AnimatedVisibility(
                visible = radio.isFavorite || isHovered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (radio.isFavorite) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (radio.isFavorite) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty state for radios.
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
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )

            Text(
                text = when {
                    showOnlyFavorites -> "No favorite radios"
                    hasFilters -> "No radios found"
                    else -> "No radios available"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (hasFilters) {
                Text(
                    text = "Try changing the filters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}


