package com.example.pokemonapp.feature.pokemon_list.domain.model

data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String> = emptyList(),
    val isFavorite: Boolean = false
) 