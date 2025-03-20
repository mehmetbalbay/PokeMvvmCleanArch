package com.example.pokemonapp.feature.pokemon_list.domain.model

data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val isFavorite: Boolean = false,
    val height: Int = 0,
    val weight: Int = 0,
    val stats: Map<String, Int> = emptyMap(),
    val abilities: List<String> = emptyList()
) 