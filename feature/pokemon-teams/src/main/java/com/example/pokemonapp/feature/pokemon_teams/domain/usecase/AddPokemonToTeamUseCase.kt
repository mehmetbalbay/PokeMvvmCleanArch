package com.example.pokemonapp.feature.pokemon_teams.domain.usecase

import com.example.pokemonapp.core.common.Result
import com.example.pokemonapp.feature.pokemon_teams.domain.model.TeamPokemon
import com.example.pokemonapp.feature.pokemon_teams.domain.repository.PokemonTeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Bir takıma Pokemon ekleme kullanım durumu
 */
class AddPokemonToTeamUseCase @Inject constructor(
    private val repository: PokemonTeamRepository
) {
    operator fun invoke(teamId: String, pokemon: TeamPokemon): Flow<Result<Unit>> = flow {
        emit(Result.loading())
        try {
            // Takımın varlığını kontrol et
            val team = repository.getTeamById(teamId)
            if (team == null) {
                emit(Result.error(IllegalArgumentException("Takım bulunamadı")))
                return@flow
            }
            
            // Takımda yeterli yer olup olmadığını kontrol et
            val currentSize = repository.getTeamSize(teamId)
            if (currentSize >= 6) {
                emit(Result.error(IllegalArgumentException("Bir takımda en fazla 6 Pokemon olabilir")))
                return@flow
            }
            
            // Aynı Pokemon'un takımda olup olmadığını kontrol et
            val hasPokemon = team.pokemons.any { it.id == pokemon.id }
            if (hasPokemon) {
                emit(Result.error(IllegalArgumentException("Bu Pokemon zaten takımda")))
                return@flow
            }
            
            // Pokemon'u sıradaki pozisyona ekle
            val pokemonWithOrder = pokemon.copy(order = currentSize)
            repository.addPokemonToTeam(teamId, pokemonWithOrder)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }
} 