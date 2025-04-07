package com.example.pokemonapp.feature.pokemon_teams.domain.usecase

import com.example.pokemonapp.core.common.resource.Resource
import com.example.pokemonapp.feature.pokemon_teams.domain.repository.PokemonTeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

/**
 * Takımdan Pokemon kaldırma kullanım durumu
 */
class RemovePokemonFromTeamUseCase @Inject constructor(
    private val repository: PokemonTeamRepository
) {
    /**
     * Belirtilen takımdan belirtilen Pokemon'u kaldırır
     * @param teamId Takım kimliği
     * @param pokemonId Pokemon kimliği
     * @return İşlemin sonucu
     */
    operator fun invoke(teamId: String, pokemonId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            
            // Takımın varlığını kontrol et
            val team = repository.getTeam(teamId)
            if (team == null) {
                emit(Resource.Error(message = "Takım bulunamadı."))
                return@flow
            }
            
            // Pokemon'un takımda olup olmadığını kontrol et
            val teamPokemon = team.pokemons.find { it.pokemonId == pokemonId }
            if (teamPokemon == null) {
                emit(Resource.Error(message = "Bu Pokemon takımda bulunamadı."))
                return@flow
            }
            
            // Pokemon'u takımdan kaldır
            val result = repository.removePokemonFromTeam(teamId, pokemonId)
            
            if (result) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(message = "Pokemon kaldırılırken bir hata oluştu."))
            }
        } catch (e: IOException) {
            emit(Resource.Error(message = "Ağ hatası: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Beklenmeyen bir hata oluştu: ${e.localizedMessage}"))
        }
    }
} 