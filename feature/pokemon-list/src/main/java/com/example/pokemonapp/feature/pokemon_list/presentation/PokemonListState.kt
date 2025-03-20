package com.example.pokemonapp.feature.pokemon_list.presentation

import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon

/**
 * Pokemon listesi ekranının durumunu temsil eden sınıf
 */
data class PokemonListState(
    val pokemons: List<Pokemon> = emptyList(),
    val filteredPokemons: List<Pokemon> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false,
    val searchQuery: String = "",
    val selectedTypes: List<String> = emptyList(),
    val showFavoritesOnly: Boolean = false,
    val sortOrder: SortOrder = SortOrder.ID,
    val currentOffset: Int = 0,
    val limit: Int = 20,
    val totalCount: Int = 0,
    val hasMoreItems: Boolean = true,
    val favoriteIds: List<Int> = emptyList(),
    val favoriteActionMessage: String? = null,
    val favoriteActionInProgress: Set<Int> = emptySet()
)

/**
 * Pokemon listesinin sıralama düzenini temsil eden enum sınıfı
 */
enum class SortOrder {
    ID,
    NAME_ASC,
    NAME_DESC,
    TYPE
} 