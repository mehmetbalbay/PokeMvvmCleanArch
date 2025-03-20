package com.example.pokemonapp.feature.pokemon_list.data.remote

import com.example.pokemonapp.feature.pokemon_list.data.remote.dto.PokemonDetailDto
import com.example.pokemonapp.feature.pokemon_list.data.remote.dto.PokemonResponse

/**
 * Pokemon'lar için uzak veri kaynağı arayüzü.
 * API işlemlerini soyutlar.
 */
interface PokemonRemoteDataSource {
    suspend fun getPokemons(offset: Int, limit: Int): PokemonResponse
    suspend fun getPokemonDetail(id: Int): PokemonDetailDto
} 