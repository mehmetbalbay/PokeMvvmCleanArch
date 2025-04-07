package com.example.pokemonapp.feature.pokemon_teams.presentation

import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam

/**
 * Takım listesi ekranının UI durumunu temsil eden sealed class
 */
sealed class TeamListUiState {
    /**
     * Yükleniyor durumu
     */
    object Loading : TeamListUiState()
    
    /**
     * Başarılı durumu - takımlar yüklendi
     * @property teams Yüklenen takımlar listesi
     * @property showCreateDialog Takım oluşturma diyaloğunun görünürlüğü
     */
    data class Success(
        val teams: List<PokemonTeam> = emptyList(),
        val showCreateDialog: Boolean = false
    ) : TeamListUiState()
    
    /**
     * Hata durumu
     * @property message Hata mesajı
     */
    data class Error(val message: String) : TeamListUiState()
} 