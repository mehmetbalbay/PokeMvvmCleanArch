package com.example.pokemonapp.feature.pokemon_list.data.remote

import com.example.pokemonapp.feature.pokemon_list.data.remote.dto.PokemonDetailDto
import com.example.pokemonapp.feature.pokemon_list.data.remote.dto.PokemonResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PokemonRemoteDataSource arayüzünün Retrofit kullanarak implementasyonu.
 */
@Singleton
class PokemonRemoteDataSourceImpl @Inject constructor(
    private val api: PokemonApi
) : PokemonRemoteDataSource {
    
    override suspend fun getPokemons(offset: Int, limit: Int): PokemonResponse {
        return api.getPokemons(offset, limit)
    }
    
    override suspend fun getPokemonDetail(id: Int): PokemonDetailDto {
        return api.getPokemonDetail(id)
    }
} 