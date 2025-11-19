package com.musicmusic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Lista lazy optimizada con scroll infinito y paginación
 * 
 * Features:
 * - Virtual scrolling (solo renderiza items visibles)
 * - Paginación automática al llegar al final
 * - Indicadores de carga
 * - Manejo de estados vacíos/error
 */
@Composable
fun <T> OptimizedLazyColumn(
    items: List<T>,
    isLoading: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    emptyMessage: String = "No hay elementos",
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    itemContent: @Composable (T) -> Unit
) {
    // Detectar cuando llega al final
    LaunchedEffect(state.isScrollInProgress) {
        snapshotFlow { state.layoutInfo }
            .collect { layoutInfo ->
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                val totalItems = layoutInfo.totalItemsCount
                
                if (lastVisibleItem != null && !isLoading && hasMore) {
                    if (lastVisibleItem.index >= totalItems - 3) {
                        onLoadMore()
                    }
                }
            }
    }
    
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            itemContent(item)
        }
        
        // Indicador de carga al final
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Mensaje vacío
        if (items.isEmpty() && !isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Grid lazy optimizado con las mismas características
 */
@Composable
fun <T> OptimizedLazyGrid(
    items: List<T>,
    columns: GridCells = GridCells.Adaptive(150.dp),
    isLoading: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    emptyMessage: String = "No hay elementos",
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    itemContent: @Composable (T) -> Unit
) {
    // Detectar cuando llega al final
    LaunchedEffect(state.isScrollInProgress) {
        snapshotFlow { state.layoutInfo }
            .collect { layoutInfo ->
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                val totalItems = layoutInfo.totalItemsCount
                
                if (lastVisibleItem != null && !isLoading && hasMore) {
                    if (lastVisibleItem.index >= totalItems - 6) {
                        onLoadMore()
                    }
                }
            }
    }
    
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            itemContent(item)
        }
        
        // Indicador de carga
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Mensaje vacío
        if (items.isEmpty() && !isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
