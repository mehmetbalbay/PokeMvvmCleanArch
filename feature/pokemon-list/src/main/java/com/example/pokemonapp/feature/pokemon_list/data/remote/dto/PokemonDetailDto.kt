package com.example.pokemonapp.feature.pokemon_list.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PokemonDetailDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("types")
    val types: List<TypeDto>,
    @SerializedName("sprites")
    val sprites: SpritesDto
)

data class TypeDto(
    @SerializedName("slot")
    val slot: Int,
    @SerializedName("type")
    val type: TypeNameDto
)

data class TypeNameDto(
    @SerializedName("name")
    val name: String
)

data class SpritesDto(
    @SerializedName("front_default")
    val frontDefault: String
) 