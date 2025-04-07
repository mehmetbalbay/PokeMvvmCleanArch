package com.example.pokemonapp.feature.pokemon_list.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PokemonListRoute(
    onPokemonClick: (Int) -> Unit,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableTypes by viewModel.availableTypes.collectAsStateWithLifecycle()
    
    PokemonListScreen(
        uiState = uiState,
        onPokemonClick = onPokemonClick,
        onRetry = viewModel::loadPokemons,
        onSearchQueryChange = viewModel::setSearchQuery,
        onSearch = viewModel::searchPokemons,
        onToggleFavorite = { pokemonId, _ ->
            viewModel.toggleFavorite(pokemonId)
        },
        onTypeSelected = viewModel::setSelectedType,
        selectedType = (uiState as? PokemonListUiState.Success)?.selectedType,
        availableTypes = availableTypes,
        viewModel = viewModel
    )
} 