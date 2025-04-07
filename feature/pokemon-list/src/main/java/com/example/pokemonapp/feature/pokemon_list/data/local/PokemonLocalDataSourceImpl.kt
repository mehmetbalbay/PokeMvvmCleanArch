package com.example.pokemonapp.feature.pokemon_list.data.local

import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.core.database.entities.FavoritePokemonEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PokemonLocalDataSource arayüzünün Room DAO kullanarak implementasyonu.
 */
@Singleton
class PokemonLocalDataSourceImpl @Inject constructor(
    private val favoritePokemonDao: FavoritePokemonDao
) : PokemonLocalDataSource {
    
    override suspend fun addToFavorites(id: Int) = withContext(Dispatchers.IO) {
        favoritePokemonDao.addToFavorites(FavoritePokemonEntity(pokemonId = id))
    }
    
    override suspend fun removeFromFavorites(id: Int) = withContext(Dispatchers.IO) {
        favoritePokemonDao.removeFromFavorites(id)
    }
    
    override suspend fun isFavorite(id: Int): Boolean = withContext(Dispatchers.IO) {
        favoritePokemonDao.isFavorite(id)
    }
    
    override suspend fun getFavorites(): List<Int> = withContext(Dispatchers.IO) {
        favoritePokemonDao.getAllFavorites().first().map { it.pokemonId }
    }
    
    override fun observeFavorite(id: Int): Flow<Boolean> {
        // ID'nin favori listesinde olup olmadığını kontrol eden bir Flow döndür
        return favoritePokemonDao.observeFavorite(id)
    }
    
    override fun observeAllFavorites(): Flow<List<Int>> {
        return favoritePokemonDao.getAllFavorites().map { favoriteEntities ->
            favoriteEntities.map { it.pokemonId }
        }
    }
} 