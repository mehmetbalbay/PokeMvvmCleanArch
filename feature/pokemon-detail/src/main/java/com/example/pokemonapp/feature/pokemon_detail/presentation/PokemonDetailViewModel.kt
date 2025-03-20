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

data class PokemonDetailState(
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
    val state: StateFlow<PokemonDetailState> = _state.asStateFlow()

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    init {
        loadPokemonDetail()
        observeFavoriteStatus()
    }

    fun loadPokemonDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val pokemonDetail = getPokemonDetailUseCase(pokemonId)
                _state.update {
                    it.copy(
                        pokemonDetail = pokemonDetail,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Pokémon detayları yüklenirken bir hata oluştu"
                    )
                }
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
                        // Favori aksiyon mesajının sınırlı süreyle gösterilmesini sağla
                        favoriteActionMessage = if (currentState.favoriteActionMessage != null && currentState.favoriteActionInProgress) {
                            if (isFavorite) "Pokémon favorilere eklendi" else "Pokémon favorilerden çıkarıldı"
                        } else null,
                        favoriteActionInProgress = false
                    )
                }
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
                
                // Optimistik UI güncellemesi
                _state.update { 
                    val updatedPokemon = pokemon.copy(isFavorite = !pokemon.isFavorite)
                    it.copy(pokemonDetail = updatedPokemon)
                }
                
                try {
                    // Veritabanında favori durumunu değiştir
                    repository.toggleFavorite(pokemon.id)
                    // Not: Artık observe ettiğimiz için, veritabanı değiştiğinde Flow yoluyla tekrar güncellenecek
                } catch (e: Exception) {
                    // Hata durumunda eski değere geri dön
                    _state.update { 
                        val revertedPokemon = _state.value.pokemonDetail?.copy(isFavorite = pokemon.isFavorite)
                        it.copy(
                            pokemonDetail = revertedPokemon,
                            error = "Favori durumu güncellenirken bir hata oluştu",
                            favoriteActionInProgress = false,
                            favoriteActionMessage = "Favori işlemi başarısız oldu: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        loadPokemonDetail()
    }
    
    fun clearFavoriteMessage() {
        _state.update { it.copy(favoriteActionMessage = null) }
    }
} 