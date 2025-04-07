package com.example.pokemonapp.feature.pokemon_teams.domain.usecase

import com.example.pokemonapp.core.common.Result
import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam
import com.example.pokemonapp.feature.pokemon_teams.domain.repository.PokemonTeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Yeni bir Pokemon takımı oluşturma kullanım durumu
 */
class CreateTeamUseCase @Inject constructor(
    private val repository: PokemonTeamRepository
) {
    operator fun invoke(team: PokemonTeam): Flow<Result<String>> = flow {
        emit(Result.loading())
        try {
            // Takım adının boş olmamasını kontrol et
            if (team.name.isBlank()) {
                emit(Result.error(IllegalArgumentException("Takım adı boş olamaz")))
                return@flow
            }
            
            // Maksimum 6 Pokemon olabilir
            if (team.pokemons.size > 6) {
                emit(Result.error(IllegalArgumentException("Bir takımda en fazla 6 Pokemon olabilir")))
                return@flow
            }
            
            // Takım oluştur
            val teamId = repository.createTeam(team)
            emit(Result.success(teamId))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }
} 