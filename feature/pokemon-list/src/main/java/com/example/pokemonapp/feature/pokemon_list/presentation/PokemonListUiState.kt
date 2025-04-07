package com.example.pokemonapp.feature.pokemon_list.presentation

import com.example.pokemonapp.core.ui.model.PokemonItemUiModel

/**
 * Pokemon listesi ekranının UI durumunu temsil eden sealed class.
 * MVI mimarisine uygun olarak tasarlanmıştır.
 */
sealed class PokemonListUiState {
    /**
     * Yükleniyor durumu
     */
    object Loading : PokemonListUiState()

    /**
     * Başarılı durum - veriler yüklendi
     */
    data class Success(
        val pokemons: List<PokemonItemUiModel>,
        val searchQuery: String = "",
        val selectedType: String? = null,
        val scrollToTop: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMoreItems: Boolean = true
    ) : PokemonListUiState()

    /**
     * Hata durumu
     */
    data class Error(
        val message: String
    ) : PokemonListUiState()
} 