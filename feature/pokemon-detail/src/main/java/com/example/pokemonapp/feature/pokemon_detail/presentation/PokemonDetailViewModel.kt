package com.example.pokemonapp.feature.pokemon_detail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonapp.feature.pokemon_detail.domain.model.PokemonDetail
import com.example.pokemonapp.feature.pokemon_detail.domain.repository.PokemonDetailRepository
import com.example.pokemonapp.feature.pokemon_detail.domain.usecase.GetPokemonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// İç durum takibi için kullanılacak
private data class PokemonDetailState(
    val pokemonDetail: PokemonDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteActionInProgress: Boolean = false,
    val favoriteActionMessage: String? = null
)

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val repository: PokemonDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonDetailState(isLoading = true))
    
    // UI State
    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    init {
        loadPokemonDetail()
        observeFavoriteStatus()
    }

    fun loadPokemonDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            _uiState.value = PokemonDetailUiState.Loading
            
            try {
                val pokemonDetail = getPokemonDetailUseCase(pokemonId)
                _state.update {
                    it.copy(
                        pokemonDetail = pokemonDetail,
                        isLoading = false,
                        error = null
                    )
                }
                
                _uiState.value = PokemonDetailUiState.Success(
                    pokemon = pokemonDetail
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Pokémon detayları yüklenirken bir hata oluştu"
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                
                _uiState.value = PokemonDetailUiState.Error(
                    message = errorMessage,
                    pokemonId = pokemonId
                )
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            repository.observeFavorite(pokemonId).collectLatest { isFavorite ->
                _state.update { currentState ->
                    val updatedPokemon = currentState.pokemonDetail?.copy(isFavorite = isFavorite)
                    currentState.copy(
                        pokemonDetail = updatedPokemon,
                        favoriteActionMessage = if (currentState.favoriteActionInProgress) {
                            if (isFavorite) "Pokémon favorilere eklendi" else "Pokémon favorilerden çıkarıldı"
                        } else null,
                        favoriteActionInProgress = false
                    )
                }
                
                // UI State güncelle
                updateUiState()
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _state.value.pokemonDetail?.let { pokemon ->
                // Aynı anda birden fazla istek oluşmasını önlemek için
                if (_state.value.favoriteActionInProgress) return@launch
                
                // Aksiyon sırasında durumu güncelle
                _state.update { 
                    it.copy(
                        favoriteActionInProgress = true,
                        favoriteActionMessage = "Favori durumu güncelleniyor..."
                    )
                }
                
                // UI State güncelle
                updateFavoriteActionUiState("Favori durumu güncelleniyor...")
                
                try {
                    // Önce veritabanında değişikliği yap
                    val newFavoriteStatus = repository.toggleFavorite(pokemon.id)
                    
                    // Optimistik UI güncellemesi
                    _state.update { 
                        val updatedPokemon = pokemon.copy(isFavorite = newFavoriteStatus)
                        it.copy(
                            pokemonDetail = updatedPokemon,
                            favoriteActionInProgress = false,
                            favoriteActionMessage = if (newFavoriteStatus) 
                                "Pokémon favorilere eklendi" 
                            else 
                                "Pokémon favorilerden çıkarıldı"
                        )
                    }
                    
                    // UI State güncelle - Flow otomatik olarak güncelleyecek ancak hemen güncellemek için
                    updateUiState()
                    
                } catch (e: Exception) {
                    // Hata durumunda eski değere geri dön
                    val errorMessage = "Favori işlemi başarısız oldu: ${e.message}"
                    _state.update { 
                        it.copy(
                            error = "Favori durumu güncellenirken bir hata oluştu",
                            favoriteActionInProgress = false,
                            favoriteActionMessage = errorMessage
                        )
                    }
                    
                    // UI State güncelle
                    updateFavoriteActionUiState(errorMessage)
                }
            }
        }
    }

    fun refresh() {
        loadPokemonDetail()
    }
    
    fun clearFavoriteMessage() {
        _state.update { it.copy(favoriteActionMessage = null) }
        updateUiState()
    }
    
    // UI State'i güncelle
    private fun updateUiState() {
        val currentState = _state.value
        
        currentState.pokemonDetail?.let { pokemonDetail ->
            _uiState.value = PokemonDetailUiState.Success(
                pokemon = pokemonDetail,
                favoriteActionMessage = currentState.favoriteActionMessage,
                favoriteActionInProgress = currentState.favoriteActionInProgress
            )
        }
    }
    
    // Favori aksiyonu için UI State'i güncelle
    private fun updateFavoriteActionUiState(message: String) {
        val currentUiState = _uiState.value
        
        if (currentUiState is PokemonDetailUiState.Success) {
            _uiState.value = currentUiState.copy(
                favoriteActionMessage = message,
                favoriteActionInProgress = true
            )
        }
    }
} 