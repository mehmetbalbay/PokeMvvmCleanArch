package com.example.pokemonapp.core.ui.model

data class PokemonItemUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val height: Float = 0f,
    val weight: Float = 0f,
    val stats: Map<String, Int> = emptyMap(),
    val isFavorite: Boolean = false
) 