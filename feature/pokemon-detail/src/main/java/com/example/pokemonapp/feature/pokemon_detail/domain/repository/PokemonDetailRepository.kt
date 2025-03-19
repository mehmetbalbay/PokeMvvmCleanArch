package com.example.pokemonapp.feature.pokemon_detail.domain.repository

import com.example.pokemonapp.feature.pokemon_detail.domain.model.PokemonDetail
import kotlinx.coroutines.flow.Flow

interface PokemonDetailRepository {
    suspend fun getPokemonDetail(id: Int): PokemonDetail
    suspend fun toggleFavorite(id: Int): Boolean
    fun observeFavorite(id: Int): Flow<Boolean>
} 