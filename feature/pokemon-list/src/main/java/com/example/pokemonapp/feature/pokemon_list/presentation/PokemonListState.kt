package com.example.pokemonapp.feature.pokemon_list.presentation

import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon

data class PokemonListState(
    val pokemons: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedType: String? = null,
    val showFavoritesOnly: Boolean = false,
    val sortOrder: SortOrder = SortOrder.ID
)

enum class SortOrder {
    ID,
    NAME_ASC,
    NAME_DESC
} 