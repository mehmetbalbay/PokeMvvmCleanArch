package com.example.pokemonapp.feature.pokemon_list.data.remote

import com.example.pokemonapp.feature.pokemon_list.data.remote.model.PokemonListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PokemonApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse
} 