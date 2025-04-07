package com.example.pokemonapp.feature.pokemon_teams.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonapp.core.common.resource.Resource
import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam
import com.example.pokemonapp.feature.pokemon_teams.domain.usecase.CreateTeamUseCase
import com.example.pokemonapp.feature.pokemon_teams.domain.usecase.GetTeamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Takım listesi ekranı için ViewModel
 */
@HiltViewModel
class TeamListViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val createTeamUseCase: CreateTeamUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TeamListUiState>(TeamListUiState.Loading)
    val uiState: StateFlow<TeamListUiState> = _uiState.asStateFlow()
    
    /**
     * Takımları yükler
     */
    fun loadTeams() {
        viewModelScope.launch {
            _uiState.value = TeamListUiState.Loading
            
            getTeamsUseCase().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = TeamListUiState.Loading
                    }
                    
                    is Resource.Success -> {
                        _uiState.value = TeamListUiState.Success(
                            teams = result.data ?: emptyList()
                        )
                    }
                    
                    is Resource.Error -> {
                        _uiState.value = TeamListUiState.Error(
                            message = result.message ?: "Takımlar yüklenirken bir hata oluştu"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Takım oluşturma diyaloğunu gösterir
     */
    fun showCreateDialog() {
        val currentState = _uiState.value
        if (currentState is TeamListUiState.Success) {
            _uiState.update { 
                TeamListUiState.Success(
                    teams = currentState.teams,
                    showCreateDialog = true
                )
            }
        }
    }
    
    /**
     * Takım oluşturma diyaloğunu gizler
     */
    fun hideCreateDialog() {
        val currentState = _uiState.value
        if (currentState is TeamListUiState.Success) {
            _uiState.update { 
                TeamListUiState.Success(
                    teams = currentState.teams,
                    showCreateDialog = false
                )
            }
        }
    }
    
    /**
     * Yeni takım oluşturur
     */
    fun createTeam(name: String, description: String) {
        viewModelScope.launch {
            val team = PokemonTeam(
                name = name,
                description = description
            )
            
            createTeamUseCase(team).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Yükleniyor durumunu göstermek istiyorsak burada durum güncellenebilir
                    }
                    
                    is Resource.Success -> {
                        // Takım başarıyla oluşturuldu, diyaloğu gizle ve takımları yeniden yükle
                        hideCreateDialog()
                        loadTeams()
                    }
                    
                    is Resource.Error -> {
                        // Hata oluştu, hata mesajını göster
                        _uiState.value = TeamListUiState.Error(
                            message = result.message ?: "Takım oluşturulurken bir hata oluştu"
                        )
                    }
                }
            }
        }
    }
} 