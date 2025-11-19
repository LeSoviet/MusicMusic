package com.musicmusic.ui.screens.radio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.musicmusic.data.repository.RadioRepository
import com.musicmusic.domain.model.Radio
import com.musicmusic.ui.screens.player.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de radios.
 */
class RadioViewModel(
    private val radioRepository: RadioRepository,
    private val playerViewModel: PlayerViewModel,
    private val viewModelScope: CoroutineScope
) {
    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Lista de radios
    private val _radios = MutableStateFlow<List<Radio>>(emptyList())
    val radios: StateFlow<List<Radio>> = _radios.asStateFlow()
    
    // Radio actualmente seleccionada
    private val _selectedRadio = MutableStateFlow<Radio?>(null)
    val selectedRadio: StateFlow<Radio?> = _selectedRadio.asStateFlow()
    
    // Búsqueda
    var searchQuery by mutableStateOf("")
        private set
    
    // Filtros
    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()
    
    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry: StateFlow<String?> = _selectedCountry.asStateFlow()
    
    // Géneros y países disponibles
    private val _availableGenres = MutableStateFlow<List<String>>(emptyList())
    val availableGenres: StateFlow<List<String>> = _availableGenres.asStateFlow()
    
    private val _availableCountries = MutableStateFlow<List<String>>(emptyList())
    val availableCountries: StateFlow<List<String>> = _availableCountries.asStateFlow()
    
    // Vista actual (todas, favoritas)
    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()
    
    init {
        loadRadios()
        observeRadioChanges()
    }
    
    /**
     * Carga las radios desde el repositorio.
     */
    private fun loadRadios() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                radioRepository.loadRadios()
                loadFilters()
                applyFilters()
            } catch (e: Exception) {
                println("Error cargando radios: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Observa cambios en las radios del repositorio.
     */
    private fun observeRadioChanges() {
        viewModelScope.launch {
            radioRepository.radios.collect { radios ->
                if (_showOnlyFavorites.value) {
                    _radios.value = radios.filter { it.isFavorite }
                } else {
                    applyFilters()
                }
            }
        }
    }
    
    /**
     * Carga los filtros disponibles (géneros y países).
     */
    private fun loadFilters() {
        viewModelScope.launch {
            _availableGenres.value = radioRepository.getAvailableGenres()
            _availableCountries.value = radioRepository.getAvailableCountries()
        }
    }
    
    /**
     * Aplica los filtros actuales.
     */
    private fun applyFilters() {
        viewModelScope.launch {
            val genre = _selectedGenre.value
            val country = _selectedCountry.value
            
            val filteredRadios = when {
                genre != null && country != null -> {
                    radioRepository.getAllRadios()
                        .filter { it.genre == genre && it.country == country }
                }
                genre != null -> radioRepository.getRadiosByGenre(genre)
                country != null -> radioRepository.getRadiosByCountry(country)
                else -> radioRepository.getAllRadios()
            }
            
            _radios.value = if (searchQuery.isNotBlank()) {
                filteredRadios.filter { radio ->
                    radio.name.contains(searchQuery, ignoreCase = true) ||
                    radio.genre?.contains(searchQuery, ignoreCase = true) == true ||
                    radio.country?.contains(searchQuery, ignoreCase = true) == true
                }
            } else {
                filteredRadios
            }
        }
    }
    
    /**
     * Actualiza la consulta de búsqueda.
     */
    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applyFilters()
    }
    
    /**
     * Selecciona un género para filtrar.
     */
    fun selectGenre(genre: String?) {
        _selectedGenre.value = genre
        applyFilters()
    }
    
    /**
     * Selecciona un país para filtrar.
     */
    fun selectCountry(country: String?) {
        _selectedCountry.value = country
        applyFilters()
    }
    
    /**
     * Alterna entre mostrar todas las radios o solo favoritas.
     */
    fun toggleShowFavorites() {
        viewModelScope.launch {
            _showOnlyFavorites.value = !_showOnlyFavorites.value
            if (_showOnlyFavorites.value) {
                _radios.value = radioRepository.getFavoriteRadios()
            } else {
                applyFilters()
            }
        }
    }
    
    /**
     * Reproduce una radio.
     */
    fun playRadio(radio: Radio) {
        viewModelScope.launch {
            try {
                _selectedRadio.value = radio
                val song = radio.toSong()
                playerViewModel.playSong(song)
            } catch (e: Exception) {
                println("Error reproduciendo radio: ${e.message}")
            }
        }
    }
    
    /**
     * Marca/desmarca una radio como favorita.
     */
    fun toggleFavorite(radioId: String) {
        viewModelScope.launch {
            radioRepository.toggleFavorite(radioId)
        }
    }
    
    /**
     * Limpia todos los filtros.
     */
    fun clearFilters() {
        searchQuery = ""
        _selectedGenre.value = null
        _selectedCountry.value = null
        applyFilters()
    }
}
