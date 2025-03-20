package com.example.pokemonapp.feature.pokemon_list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonapp.core.common.Resource
import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.GetPokemonDetailUseCase
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.GetPokemonListUseCase
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.ObserveFavoritePokemonsUseCase
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.ToggleFavoritePokemonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pokemon Listesi için ViewModel.
 * MVI mimarisini kullanarak durumu yönetir.
 */
@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val getPokemonListUseCase: GetPokemonListUseCase,
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val toggleFavoritePokemonUseCase: ToggleFavoritePokemonUseCase,
    private val observeFavoritePokemonsUseCase: ObserveFavoritePokemonsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonListState())
    val state: StateFlow<PokemonListState> = _state.asStateFlow()
    
    private val _availableTypes = MutableStateFlow<List<String>>(emptyList())
    val availableTypes: StateFlow<List<String>> = _availableTypes.asStateFlow()

    init {
        loadPokemons()
        observeFavorites()
    }

    /**
     * Pokemon listesini yükler
     */
    fun loadPokemons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            getPokemonListUseCase(offset = 0, limit = _state.value.limit)
                .catch { e ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Pokemon yüklenirken hata oluştu: ${e.message}",
                        isEmpty = true
                    )}
                }
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _state.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            val pokemons = result.data?.pokemons ?: emptyList()
                            _state.update { it.copy(
                                pokemons = pokemons,
                                isLoading = false,
                                currentOffset = _state.value.limit,
                                totalCount = result.data?.count ?: 0,
                                hasMoreItems = (_state.value.limit < (result.data?.count ?: 0)),
                                isEmpty = pokemons.isEmpty()
                            )}
                            
                            // Tip listesini güncelle
                            updateAvailableTypes()
                            
                            applyFilters()
                        }
                        is Resource.Error -> {
                            _state.update { it.copy(
                                isLoading = false,
                                error = result.message ?: "Bilinmeyen bir hata oluştu",
                                isEmpty = true
                            )}
                        }
                        is Resource.Empty -> {
                            _state.update { it.copy(
                                isLoading = false,
                                isEmpty = true,
                                error = null
                            )}
                        }
                    }
                }
        }
    }

    /**
     * Daha fazla Pokemon yükler (sayfalama)
     */
    fun loadMorePokemons() {
        val currentState = _state.value
        
        if (currentState.isLoadingMore || !currentState.hasMoreItems) return
        
        // Herhangi bir filtreleme aktifse, daha fazla yükleme yapma
        if (currentState.searchQuery.isNotEmpty() || 
            currentState.selectedTypes.isNotEmpty() || 
            currentState.showFavoritesOnly) {
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            
            getPokemonListUseCase(offset = currentState.currentOffset, limit = currentState.limit)
                .catch { e ->
                    _state.update { it.copy(
                        isLoadingMore = false,
                        error = "Daha fazla Pokemon yüklenirken hata oluştu: ${e.message}"
                    )}
                }
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            val newPokemons = result.data?.pokemons ?: emptyList()
                            val updatedPokemons = currentState.pokemons + newPokemons
                            
                            _state.update { it.copy(
                                pokemons = updatedPokemons,
                                isLoadingMore = false,
                                currentOffset = currentState.currentOffset + currentState.limit,
                                hasMoreItems = (currentState.currentOffset + currentState.limit < (result.data?.count ?: 0))
                            )}
                            
                            // Tip listesini güncelle
                            updateAvailableTypes()
                        }
                        is Resource.Error -> {
                            _state.update { it.copy(
                                isLoadingMore = false,
                                error = result.message
                            )}
                        }
                        else -> {} // Loading ve Empty durumlarını burada ele almaya gerek yok
                    }
                }
        }
    }

    /**
     * Yenile
     */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(
                isRefreshing = true,
                error = null
            )}
            
            loadPokemons()
            
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Favorileri gözlemler
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoritePokemonsUseCase().collectLatest { favoriteIds ->
                _state.update { it.copy(favoriteIds = favoriteIds) }
                updatePokemonFavoriteStatus(favoriteIds)
            }
        }
    }

    /**
     * Pokemonların favori durumunu günceller
     */
    private fun updatePokemonFavoriteStatus(favoriteIds: List<Int>) {
        val updatedPokemons = _state.value.pokemons.map { pokemon ->
            pokemon.copy(isFavorite = favoriteIds.contains(pokemon.id))
        }
        
        _state.update { it.copy(pokemons = updatedPokemons) }
    }

    /**
     * Pokemon'u favorilere ekler/çıkarır
     */
    fun toggleFavorite(id: Int) {
        // Aynı Pokemon için işlem devam ediyorsa yeni istek atma
        if (_state.value.favoriteActionInProgress.contains(id)) return
        
        // İşlem başladığını belirt
        _state.update { currentState ->
            currentState.copy(
                favoriteActionInProgress = currentState.favoriteActionInProgress + id
            )
        }
        
        viewModelScope.launch {
            toggleFavoritePokemonUseCase(id)
                .catch { e ->
                    _state.update { currentState ->
                        currentState.copy(
                            error = "Favori durumu güncellenirken hata oluştu: ${e.message}",
                            favoriteActionInProgress = currentState.favoriteActionInProgress - id,
                            favoriteActionMessage = "Favori işlemi başarısız oldu"
                        )
                    }
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val message = if (result.data == true) {
                                "Pokemon favorilere eklendi!"
                            } else {
                                "Pokemon favorilerden kaldırıldı!"
                            }
                            _state.update { currentState ->
                                currentState.copy(
                                    favoriteActionMessage = message,
                                    favoriteActionInProgress = currentState.favoriteActionInProgress - id
                                )
                            }
                        }
                        is Resource.Error -> {
                            _state.update { currentState ->
                                currentState.copy(
                                    error = result.message,
                                    favoriteActionInProgress = currentState.favoriteActionInProgress - id,
                                    favoriteActionMessage = "Favori işlemi başarısız oldu: ${result.message}"
                                )
                            }
                        }
                        else -> {
                            // Loading ve Empty durumlarında favoriteActionInProgress durumunu güncelleme
                            if (result !is Resource.Loading) {
                                _state.update { currentState ->
                                    currentState.copy(
                                        favoriteActionInProgress = currentState.favoriteActionInProgress - id
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    // Favori mesajını temizle
    fun clearFavoriteMessage() {
        _state.update { it.copy(favoriteActionMessage = null) }
    }

    /**
     * Arama sorgusunu günceller
     */
    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    /**
     * Seçili türü günceller
     */
    fun toggleTypeFilter(type: String) {
        val currentSelectedTypes = _state.value.selectedTypes.toMutableList()
        if (currentSelectedTypes.contains(type)) {
            currentSelectedTypes.remove(type)
        } else {
            currentSelectedTypes.add(type)
        }
        
        _state.update { it.copy(selectedTypes = currentSelectedTypes) }
        applyFilters()
    }
    
    /**
     * Tip filtrelerini ayarlar
     */
    fun setTypeFilters(types: List<String>) {
        _state.update { it.copy(selectedTypes = types) }
        applyFilters()
    }

    /**
     * Sadece favorileri gösterme durumunu değiştirir
     */
    fun toggleShowFavoritesOnly() {
        _state.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
        applyFilters()
    }

    /**
     * Sıralama düzenini değiştirir
     */
    fun updateSortOrder(sortOrder: SortOrder) {
        _state.update { it.copy(sortOrder = sortOrder) }
        applyFilters()
    }

    /**
     * Filtreleri uygula
     */
    private fun applyFilters() {
        val currentState = _state.value
        val allPokemons = currentState.pokemons
        var filteredPokemons = allPokemons
        
        // Arama sorgusuna göre filtrele
        if (currentState.searchQuery.isNotEmpty()) {
            val lowerCaseQuery = currentState.searchQuery.lowercase()
            filteredPokemons = filteredPokemons.filter { pokemon -> 
                // İsim içinde arama
                val nameMatch = pokemon.name.lowercase().contains(lowerCaseQuery)
                // ID içinde arama
                val idMatch = pokemon.id.toString().contains(lowerCaseQuery)
                // Tip içinde arama
                val typeMatch = pokemon.types.any { it.lowercase().contains(lowerCaseQuery) }
                
                nameMatch || idMatch || typeMatch
            }
        }
        
        // Seçili türlere göre filtrele
        if (currentState.selectedTypes.isNotEmpty()) {
            // Eşleşen TÜM tiplere sahip Pokemonları filtrele
            filteredPokemons = filteredPokemons.filter { pokemon ->
                currentState.selectedTypes.all { selectedType ->
                    pokemon.types.any { pokemonType ->
                        pokemonType.equals(selectedType, ignoreCase = true)
                    }
                }
            }
        }
        
        // Sadece favorileri göster
        if (currentState.showFavoritesOnly) {
            filteredPokemons = filteredPokemons.filter { it.isFavorite }
        }
        
        // Sıralama düzenine göre sırala
        filteredPokemons = when (currentState.sortOrder) {
            SortOrder.ID -> filteredPokemons.sortedBy { it.id }
            SortOrder.NAME_ASC -> filteredPokemons.sortedBy { it.name }
            SortOrder.NAME_DESC -> filteredPokemons.sortedByDescending { it.name }
            SortOrder.TYPE -> filteredPokemons.sortedBy { it.types.firstOrNull() ?: "" }
        }
        
        // Sonuçları yeni bir liste olarak ayarla
        _state.update { currentState ->
            currentState.copy(
                filteredPokemons = filteredPokemons,
                isEmpty = filteredPokemons.isEmpty() && !currentState.isLoading
            )
        }
    }
    
    /**
     * Pokemon verilerinden tip listesi oluştur
     */
    private fun updateAvailableTypes() {
        val allTypes = _state.value.pokemons
            .flatMap { it.types }
            .distinct()
            .sorted()
        
        _availableTypes.value = allTypes
    }
} 