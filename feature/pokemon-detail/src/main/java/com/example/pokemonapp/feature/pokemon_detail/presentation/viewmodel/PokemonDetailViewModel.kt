package com.example.pokemonapp.feature.pokemon_detail.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonapp.feature.pokemon_detail.domain.model.PokemonDetail
import com.example.pokemonapp.feature.pokemon_detail.domain.usecase.GetPokemonDetailUseCase
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val pokemonRepository: PokemonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    init {
        getPokemonDetail()
        observeFavoriteStatus()
    }
    
    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            pokemonRepository.observeFavorite(pokemonId).collectLatest { isFavorite ->
                val currentState = _uiState.value
                if (currentState is PokemonDetailUiState.Success && currentState.pokemonDetail.isFavorite != isFavorite) {
                    _uiState.value = PokemonDetailUiState.Success(currentState.pokemonDetail.copy(isFavorite = isFavorite))
                }
            }
        }
    }

    private fun getPokemonDetail() {
        viewModelScope.launch {
            try {
                val pokemonDetail = getPokemonDetailUseCase(pokemonId)
                val isFavorite = pokemonRepository.isFavorite(pokemonId)
                _uiState.value = PokemonDetailUiState.Success(pokemonDetail.copy(isFavorite = isFavorite))
            } catch (e: Exception) {
                _uiState.value = PokemonDetailUiState.Error(e.message ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val isFavorite = pokemonRepository.toggleFavorite(pokemonId)
                // Not: Artık favori değişiklikleri observeFavoriteStatus() metoduyla izleniyor
                // Ancak UI'ı hemen güncellemek için burada da düzenleme yapıyoruz
                val currentState = _uiState.value
                if (currentState is PokemonDetailUiState.Success) {
                    _uiState.value = PokemonDetailUiState.Success(currentState.pokemonDetail.copy(isFavorite = isFavorite))
                }
            } catch (e: Exception) {
                // Favori durumu değiştirirken hata oluştuğunda UI'da gösterilecek bir hata mesajı
            }
        }
    }
}

sealed class PokemonDetailUiState {
    object Loading : PokemonDetailUiState()
    data class Success(val pokemonDetail: PokemonDetail) : PokemonDetailUiState()
    data class Error(val message: String) : PokemonDetailUiState()
} 