package com.example.pokemonapp.feature.pokemon_teams.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam
import com.example.pokemonapp.feature.pokemon_teams.domain.usecase.DeleteTeamUseCase
import com.example.pokemonapp.feature.pokemon_teams.domain.usecase.GetTeamUseCase
import com.example.pokemonapp.feature.pokemon_teams.domain.usecase.RemovePokemonFromTeamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Takım detay ekranı için ViewModel
 */
@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    private val getTeamUseCase: GetTeamUseCase,
    private val removePokemonFromTeamUseCase: RemovePokemonFromTeamUseCase,
    private val deleteTeamUseCase: DeleteTeamUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TeamDetailUiState>(TeamDetailUiState.Loading)
    val uiState: StateFlow<TeamDetailUiState> = _uiState.asStateFlow()
    
    /**
     * Belirtilen ID'ye sahip takımı yükler
     */
    fun loadTeam(teamId: String) {
        viewModelScope.launch {
            _uiState.value = TeamDetailUiState.Loading
            
            getTeamUseCase(teamId).collectLatest { result ->
                when (result) {
                    is com.example.pokemonapp.core.common.resource.Resource.Loading -> {
                        _uiState.value = TeamDetailUiState.Loading
                    }
                    
                    is com.example.pokemonapp.core.common.resource.Resource.Success -> {
                        result.data?.let { team ->
                            _uiState.value = TeamDetailUiState.Success(team)
                        } ?: run {
                            _uiState.value = TeamDetailUiState.Error("Takım bulunamadı")
                        }
                    }
                    
                    is com.example.pokemonapp.core.common.resource.Resource.Error -> {
                        _uiState.value = TeamDetailUiState.Error(
                            result.message ?: "Takım yüklenirken bir hata oluştu"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Takımdan bir Pokemonu kaldırır
     */
    fun removePokemon(pokemonId: String) {
        val currentState = _uiState.value
        if (currentState !is TeamDetailUiState.Success) return
        
        viewModelScope.launch {
            val teamId = currentState.team.id
            
            removePokemonFromTeamUseCase(teamId, pokemonId).collectLatest { result ->
                when (result) {
                    is com.example.pokemonapp.core.common.resource.Resource.Loading -> {
                        // Yükleniyor durumunu göstermek istiyorsak burada durum güncellenebilir
                    }
                    
                    is com.example.pokemonapp.core.common.resource.Resource.Success -> {
                        // Pokemon başarıyla kaldırıldıktan sonra takımı yeniden yükle
                        loadTeam(teamId)
                    }
                    
                    is com.example.pokemonapp.core.common.resource.Resource.Error -> {
                        _uiState.value = TeamDetailUiState.Error(
                            result.message ?: "Pokemon kaldırılırken bir hata oluştu"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Takımı silmek için onay diyaloğunu gösterir
     */
    fun showDeleteDialog() {
        val currentState = _uiState.value
        if (currentState !is TeamDetailUiState.Success) return
        
        _uiState.update { 
            TeamDetailUiState.Success(
                team = currentState.team,
                showDeleteDialog = true
            )
        }
    }
    
    /**
     * Takımı silmek için onay diyaloğunu gizler
     */
    fun hideDeleteDialog() {
        val currentState = _uiState.value
        if (currentState !is TeamDetailUiState.Success) return
        
        _uiState.update { 
            TeamDetailUiState.Success(
                team = currentState.team,
                showDeleteDialog = false
            )
        }
    }
    
    /**
     * Takımı siler
     */
    fun deleteTeam() {
        val currentState = _uiState.value
        if (currentState !is TeamDetailUiState.Success) return
        
        viewModelScope.launch {
            val teamId = currentState.team.id
            
            deleteTeamUseCase(teamId).collectLatest { result ->
                when (result) {
                    is com.example.pokemonapp.core.common.resource.Resource.Loading -> {
                        // Yükleniyor durumunu göstermek istiyorsak burada durum güncellenebilir
                    }
                    
                    is com.example.pokemonapp.core.common.resource.Resource.Success -> {
                        // Takım başarıyla silindi, silindi durumuna geç
                        _uiState.value = TeamDetailUiState.Deleted
                    }
                    
                    is com.example.pokemonapp.core.common.resource.Resource.Error -> {
                        _uiState.value = TeamDetailUiState.Error(
                            result.message ?: "Takım silinirken bir hata oluştu"
                        )
                    }
                }
            }
        }
    }
} 