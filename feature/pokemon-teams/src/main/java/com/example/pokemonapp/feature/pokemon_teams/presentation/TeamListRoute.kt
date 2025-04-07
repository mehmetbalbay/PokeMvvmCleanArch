package com.example.pokemonapp.feature.pokemon_teams.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Takım listesi ekranı rotası
 */
@Composable
fun TeamListRoute(
    onBackClick: () -> Unit,
    onTeamClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamListViewModel = hiltViewModel()
) {
    // ViewModel'den UI durumunu al
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Ekran ilk açıldığında takımları yükle
    LaunchedEffect(key1 = true) {
        viewModel.loadTeams()
    }
    
    TeamListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTeamClick = onTeamClick,
        onCreateTeamClick = viewModel::showCreateDialog,
        onCreateTeam = viewModel::createTeam,
        onDismissCreateDialog = viewModel::hideCreateDialog,
        onRefresh = viewModel::loadTeams
    )
} 