package com.example.pokemonapp.feature.pokemon_detail.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface PokemonDetailApi {
    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: Int): PokemonDetailResponse
}

data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val sprites: Sprites,
    val types: List<TypeResponse>,
    val height: Int,
    val weight: Int,
    val stats: List<StatResponse>,
    val abilities: List<AbilityResponse>,
    val moves: List<MoveResponse> = emptyList()
)

data class Sprites(
    val front_default: String
)

data class TypeResponse(
    val type: Type
)

data class Type(
    val name: String
)

data class StatResponse(
    val base_stat: Int,
    val stat: Stat
)

data class Stat(
    val name: String
)

data class AbilityResponse(
    val ability: Ability,
    val is_hidden: Boolean
)

data class Ability(
    val name: String
)

data class MoveResponse(
    val move: Move
)

data class Move(
    val name: String
) 