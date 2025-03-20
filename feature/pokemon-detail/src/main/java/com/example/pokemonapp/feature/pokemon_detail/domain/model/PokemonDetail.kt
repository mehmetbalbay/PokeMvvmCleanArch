package com.example.pokemonapp.feature.pokemon_detail.domain.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val height: Int,
    val weight: Int,
    val stats: List<Stat>,
    val abilities: List<String>,
    val moves: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

data class Stat(
    val name: String,
    val value: Int
)

data class Ability(
    val name: String,
    val isHidden: Boolean
) 