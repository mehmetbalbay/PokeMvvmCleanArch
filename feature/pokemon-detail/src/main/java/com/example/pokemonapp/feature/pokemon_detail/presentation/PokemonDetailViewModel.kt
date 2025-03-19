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

data class PokemonDetailUiState(
    val pokemon: PokemonDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val repository: PokemonDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonDetailUiState(isLoading = true))
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    init {
        loadPokemonDetail()
        observeFavoriteStatus()
    }

    private fun loadPokemonDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val pokemonDetail = getPokemonDetailUseCase(pokemonId)
                _uiState.update {
                    it.copy(
                        pokemon = pokemonDetail,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
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
                _uiState.update { currentState ->
                    val updatedPokemon = currentState.pokemon?.copy(isFavorite = isFavorite)
                    currentState.copy(pokemon = updatedPokemon)
                }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _uiState.value.pokemon?.let { pokemon ->
                // Optimistik UI güncellemesi
                _uiState.update { 
                    val updatedPokemon = pokemon.copy(isFavorite = !pokemon.isFavorite)
                    it.copy(pokemon = updatedPokemon)
                }
                
                try {
                    // Veritabanında favori durumunu değiştir
                    repository.toggleFavorite(pokemon.id)
                    // Not: Artık observe ettiğimiz için, veritabanı değiştiğinde Flow yoluyla tekrar güncellenecek
                } catch (e: Exception) {
                    // Hata durumunda eski değere geri dön
                    _uiState.update { 
                        val revertedPokemon = _uiState.value.pokemon?.copy(isFavorite = pokemon.isFavorite)
                        it.copy(
                            pokemon = revertedPokemon,
                            error = "Favori durumu güncellenirken bir hata oluştu"
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        loadPokemonDetail()
    }
} 