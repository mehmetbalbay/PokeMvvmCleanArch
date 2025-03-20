package com.example.pokemonapp.feature.pokemon_list.domain.repository

import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

// Toplam Pokemon sayısı ve liste içeren veri sınıfı
data class PokemonListResult(
    val pokemons: List<Pokemon>,
    val count: Int
)

interface PokemonRepository {
    suspend fun getPokemons(offset: Int = 0, limit: Int = 20): List<Pokemon>
    suspend fun getPokemonsWithCount(offset: Int = 0, limit: Int = 20): PokemonListResult
    suspend fun getPokemonById(id: Int): Pokemon
    suspend fun toggleFavorite(id: Int): Boolean
    suspend fun isFavorite(id: Int): Boolean
    fun observeFavorite(id: Int): Flow<Boolean>
    fun observeAllFavorites(): Flow<List<Int>>
    suspend fun getPokemonDetail(id: Int): Pokemon
    suspend fun getFavoritePokemons(): List<Int>
    suspend fun getAllPokemonTypes(): List<String>
    suspend fun getPokemonsByTypes(types: List<String>, offset: Int, limit: Int): List<Pokemon>
} 