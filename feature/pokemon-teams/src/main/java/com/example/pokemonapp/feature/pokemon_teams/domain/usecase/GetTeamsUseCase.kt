package com.example.pokemonapp.feature.pokemon_teams.domain.usecase

import com.example.pokemonapp.core.common.Result
import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam
import com.example.pokemonapp.feature.pokemon_teams.domain.repository.PokemonTeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * Tüm Pokemon takımlarını getirme kullanım durumu
 */
class GetTeamsUseCase @Inject constructor(
    private val repository: PokemonTeamRepository
) {
    operator fun invoke(): Flow<Result<List<PokemonTeam>>> {
        return repository.getAllTeams()
            .map { teams -> Result.success(teams) }
            .onStart { emit(Result.loading()) }
            .catch { e -> emit(Result.error(e)) }
    }
} 