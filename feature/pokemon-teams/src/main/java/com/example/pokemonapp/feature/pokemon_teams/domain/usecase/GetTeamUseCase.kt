package com.example.pokemonapp.feature.pokemon_teams.domain.usecase

import com.example.pokemonapp.core.common.resource.Resource
import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam
import com.example.pokemonapp.feature.pokemon_teams.domain.repository.PokemonTeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

/**
 * Belirli bir takımı getirme kullanım durumu
 */
class GetTeamUseCase @Inject constructor(
    private val repository: PokemonTeamRepository
) {
    /**
     * Belirtilen ID'ye sahip takımı getirir
     * @param teamId Takım ID'si
     * @return Takım bilgisi
     */
    operator fun invoke(teamId: String): Flow<Resource<PokemonTeam>> = flow {
        try {
            emit(Resource.Loading())
            
            val team = repository.getTeam(teamId)
            
            if (team != null) {
                emit(Resource.Success(team))
            } else {
                emit(Resource.Error(message = "Takım bulunamadı."))
            }
        } catch (e: IOException) {
            emit(Resource.Error(message = "Ağ hatası: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Beklenmeyen bir hata oluştu: ${e.localizedMessage}"))
        }
    }
} 