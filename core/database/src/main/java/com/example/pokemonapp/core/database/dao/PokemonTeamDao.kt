package com.example.pokemonapp.core.database.dao

import androidx.room.*
import com.example.pokemonapp.core.database.entities.PokemonTeamEntity
import com.example.pokemonapp.core.database.entities.TeamPokemonEntity
import kotlinx.coroutines.flow.Flow

/**
 * Pokemon takımları için veri erişim sınıfı
 */
@Dao
interface PokemonTeamDao {
    // Takım işlemleri
    @Query("SELECT * FROM pokemon_teams ORDER BY updated_at DESC")
    fun getAllTeams(): Flow<List<PokemonTeamEntity>>
    
    @Query("SELECT * FROM pokemon_teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: String): PokemonTeamEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: PokemonTeamEntity): Long
    
    @Update
    suspend fun updateTeam(team: PokemonTeamEntity)
    
    @Delete
    suspend fun deleteTeam(team: PokemonTeamEntity)
    
    @Query("DELETE FROM pokemon_teams WHERE id = :teamId")
    suspend fun deleteTeamById(teamId: String)
    
    // Takımdaki Pokemon işlemleri
    @Query("SELECT * FROM team_pokemons WHERE team_id = :teamId ORDER BY `order`")
    fun getPokemonsForTeam(teamId: String): Flow<List<TeamPokemonEntity>>
    
    @Query("SELECT * FROM team_pokemons WHERE team_id = :teamId ORDER BY `order`")
    suspend fun getPokemonsForTeamSync(teamId: String): List<TeamPokemonEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamPokemon(pokemon: TeamPokemonEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamPokemons(pokemons: List<TeamPokemonEntity>)
    
    @Query("DELETE FROM team_pokemons WHERE team_id = :teamId AND pokemon_id = :pokemonId")
    suspend fun removeTeamPokemon(teamId: String, pokemonId: Int)
    
    @Query("DELETE FROM team_pokemons WHERE team_id = :teamId AND pokemon_id = :pokemonId")
    suspend fun removeTeamPokemon(teamId: String, pokemonId: String)
    
    @Query("DELETE FROM team_pokemons WHERE team_id = :teamId")
    suspend fun deleteTeamPokemons(teamId: String)
    
    @Query("SELECT COUNT(*) FROM team_pokemons WHERE team_id = :teamId")
    suspend fun getTeamSize(teamId: String): Int
    
    @Transaction
    suspend fun updateTeamWithPokemons(team: PokemonTeamEntity, pokemons: List<TeamPokemonEntity>) {
        updateTeam(team)
        
        // Mevcut Pokemonları sil ve yenilerini ekle
        deleteTeamPokemons(team.id)
        insertTeamPokemons(pokemons)
    }
} 