package com.example.pokemonapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pokemonapp.core.database.entities.FavoritePokemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePokemonDao {
    @Query("SELECT * FROM favorite_pokemons")
    fun getAllFavorites(): Flow<List<FavoritePokemonEntity>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_pokemons WHERE pokemonId = :pokemonId LIMIT 1)")
    suspend fun isFavorite(pokemonId: Int): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_pokemons WHERE pokemonId = :pokemonId LIMIT 1)")
    fun observeFavorite(pokemonId: Int): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favoritePokemon: FavoritePokemonEntity)
    
    @Query("DELETE FROM favorite_pokemons WHERE pokemonId = :pokemonId")
    suspend fun removeFromFavorites(pokemonId: Int)
} 