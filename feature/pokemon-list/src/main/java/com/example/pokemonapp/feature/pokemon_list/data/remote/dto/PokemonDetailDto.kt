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
    val sprites: SpritesDto,
    @SerializedName("height")
    val height: Int,
    @SerializedName("weight")
    val weight: Int,
    @SerializedName("stats")
    val stats: List<StatDto>,
    @SerializedName("abilities")
    val abilities: List<AbilityDto>
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

data class StatDto(
    @SerializedName("base_stat")
    val baseStat: Int,
    @SerializedName("stat")
    val stat: StatNameDto
)

data class StatNameDto(
    @SerializedName("name")
    val name: String
)

data class AbilityDto(
    @SerializedName("ability")
    val ability: AbilityNameDto,
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    @SerializedName("slot")
    val slot: Int
)

data class AbilityNameDto(
    @SerializedName("name")
    val name: String
) 