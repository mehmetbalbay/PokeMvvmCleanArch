package com.example.pokemonapp.feature.pokemon_detail.presentation

import com.example.pokemonapp.feature.pokemon_detail.domain.model.PokemonDetail

/**
 * Pokemon detay ekranının UI durumunu temsil eden sealed class.
 * MVI mimarisine uygun olarak tasarlanmıştır.
 */
sealed class PokemonDetailUiState {
    /**
     * Yükleniyor durumu
     */
    object Loading : PokemonDetailUiState()

    /**
     * Başarılı durum - veriler yüklendi
     */
    data class Success(
        val pokemon: PokemonDetail,
        val favoriteActionMessage: String? = null,
        val favoriteActionInProgress: Boolean = false
    ) : PokemonDetailUiState()

    /**
     * Hata durumu
     */
    data class Error(
        val message: String,
        val pokemonId: Int? = null
    ) : PokemonDetailUiState()
} 