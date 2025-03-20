package com.example.pokemonapp.feature.pokemon_list.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PokemonLocalDataSource arayüzünün Room DAO kullanarak implementasyonu.
 */
@Singleton
class PokemonLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PokemonLocalDataSource {
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Favori Pokémon ID'lerinin listesi
    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    
    init {
        // SharedPreferences'dan favori Pokémon ID'lerini yükle
        val favoriteIds = sharedPreferences.getStringSet(KEY_FAVORITES, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
        
        _favorites.value = favoriteIds
    }
    
    override suspend fun addToFavorites(id: Int) = withContext(Dispatchers.IO) {
        val currentFavorites = _favorites.value.toMutableSet()
        currentFavorites.add(id)
        _favorites.value = currentFavorites
        
        // SharedPreferences'a kaydet
        sharedPreferences.edit()
            .putStringSet(KEY_FAVORITES, currentFavorites.map { it.toString() }.toSet())
            .apply()
    }
    
    override suspend fun removeFromFavorites(id: Int) = withContext(Dispatchers.IO) {
        val currentFavorites = _favorites.value.toMutableSet()
        currentFavorites.remove(id)
        _favorites.value = currentFavorites
        
        // SharedPreferences'a kaydet
        sharedPreferences.edit()
            .putStringSet(KEY_FAVORITES, currentFavorites.map { it.toString() }.toSet())
            .apply()
    }
    
    override suspend fun isFavorite(id: Int): Boolean = withContext(Dispatchers.IO) {
        _favorites.value.contains(id)
    }
    
    override suspend fun getFavorites(): List<Int> = withContext(Dispatchers.IO) {
        _favorites.value.toList()
    }
    
    override fun observeFavorite(id: Int): Flow<Boolean> {
        // ID'nin favori listesinde olup olmadığını kontrol eden bir Flow döndür
        return _favorites.asStateFlow().map { favorites ->
            favorites.contains(id)
        }
    }
    
    override fun observeAllFavorites(): Flow<List<Int>> {
        return _favorites.asStateFlow().map { it.toList() }
    }
    
    companion object {
        private const val PREFS_NAME = "pokemon_preferences"
        private const val KEY_FAVORITES = "favorite_pokemon_ids"
    }
} 