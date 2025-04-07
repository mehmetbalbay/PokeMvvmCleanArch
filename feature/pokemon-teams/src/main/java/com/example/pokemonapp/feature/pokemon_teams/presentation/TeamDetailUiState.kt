package com.example.pokemonapp.feature.pokemon_teams.presentation

import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam

/**
 * Takım detay ekranının UI durumunu temsil eden sealed class
 */
sealed class TeamDetailUiState {
    /**
     * Yükleniyor durumu
     */
    object Loading : TeamDetailUiState()
    
    /**
     * Başarılı durumu - takımın verileri yüklendi
     * @property team Yüklenen takım
     * @property showDeleteDialog Silme onay diyaloğunun görünürlüğü
     */
    data class Success(
        val team: PokemonTeam,
        val showDeleteDialog: Boolean = false
    ) : TeamDetailUiState()
    
    /**
     * Hata durumu
     * @property message Hata mesajı
     */
    data class Error(val message: String) : TeamDetailUiState()
    
    /**
     * Silindi durumu - takım başarıyla silindi
     */
    object Deleted : TeamDetailUiState()
} 