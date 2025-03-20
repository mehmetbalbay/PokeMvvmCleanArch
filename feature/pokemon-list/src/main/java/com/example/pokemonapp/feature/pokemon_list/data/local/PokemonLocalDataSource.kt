package com.example.pokemonapp.feature.pokemon_list.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Pokemon'lar için yerel veri kaynağı arayüzü.
 * Veritabanı işlemlerini soyutlar.
 */
interface PokemonLocalDataSource {
    /**
     * Pokemon'u favorilere ekler
     */
    suspend fun addToFavorites(id: Int)
    
    /**
     * Pokemon'u favorilerden çıkarır
     */
    suspend fun removeFromFavorites(id: Int)
    
    /**
     * Pokemon'un favori olup olmadığını kontrol eder
     */
    suspend fun isFavorite(id: Int): Boolean
    
    /**
     * Favori Pokemon'ların ID listesini döndürür
     */
    suspend fun getFavorites(): List<Int>
    
    /**
     * Belirli bir Pokémon'un favori durumunu gözlemlemek için Flow döndürür
     */
    fun observeFavorite(id: Int): Flow<Boolean>
    
    /**
     * Tüm favori Pokémon ID'lerini gözlemlemek için Flow döndürür
     */
    fun observeAllFavorites(): Flow<List<Int>>
} 