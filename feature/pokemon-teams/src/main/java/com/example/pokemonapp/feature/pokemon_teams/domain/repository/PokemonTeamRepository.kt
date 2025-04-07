package com.example.pokemonapp.feature.pokemon_teams.domain.repository

import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam
import com.example.pokemonapp.feature.pokemon_teams.domain.model.TeamPokemon
import kotlinx.coroutines.flow.Flow

/**
 * Pokemon takımlarını yönetmek için repository arayüzü
 */
interface PokemonTeamRepository {
    /**
     * Tüm takımları akış olarak getirir
     */
    fun getAllTeams(): Flow<List<PokemonTeam>>
    
    /**
     * Belirli bir takımı ID'sine göre getirir
     */
    suspend fun getTeamById(teamId: String): PokemonTeam?
    
    /**
     * GetTeamUseCase için kullanılan alternatif metod
     */
    suspend fun getTeam(teamId: String): PokemonTeam?
    
    /**
     * Bir takımın Pokemon'larını flow olarak getirir
     */
    fun getTeamPokemons(teamId: String): Flow<List<TeamPokemon>>
    
    /**
     * Yeni bir takım oluşturur
     */
    suspend fun createTeam(team: PokemonTeam): String
    
    /**
     * Var olan bir takımı günceller
     */
    suspend fun updateTeam(team: PokemonTeam)
    
    /**
     * Bir takımı siler
     */
    suspend fun deleteTeam(teamId: String): Boolean
    
    /**
     * Takıma bir Pokemon ekler
     */
    suspend fun addPokemonToTeam(teamId: String, pokemon: TeamPokemon)
    
    /**
     * Takımdan bir Pokemon'u kaldırır (int parametreli)
     */
    suspend fun removePokemonFromTeam(teamId: String, pokemonId: Int)
    
    /**
     * Takımdan bir Pokemon'u kaldırır (string parametreli)
     */
    suspend fun removePokemonFromTeam(teamId: String, pokemonId: String): Boolean
    
    /**
     * Takımdaki Pokemon'ların sırasını günceller
     */
    suspend fun reorderTeamPokemons(teamId: String, pokemonIds: List<Int>)
    
    /**
     * Takımdaki Pokemon sayısını getirir
     */
    suspend fun getTeamSize(teamId: String): Int
} 