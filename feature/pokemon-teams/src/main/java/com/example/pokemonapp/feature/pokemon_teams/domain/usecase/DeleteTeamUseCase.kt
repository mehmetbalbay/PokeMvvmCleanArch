package com.example.pokemonapp.feature.pokemon_teams.domain.usecase

import com.example.pokemonapp.core.common.resource.Resource
import com.example.pokemonapp.feature.pokemon_teams.domain.repository.PokemonTeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

/**
 * Takım silme kullanım durumu
 */
class DeleteTeamUseCase @Inject constructor(
    private val repository: PokemonTeamRepository
) {
    /**
     * Belirtilen ID'ye sahip takımı siler
     * @param teamId Silinecek takımın ID'si
     * @return İşlemin sonucu
     */
    operator fun invoke(teamId: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            
            // Takımın varlığını kontrol et
            val team = repository.getTeam(teamId)
            if (team == null) {
                emit(Resource.Error(message = "Takım bulunamadı."))
                return@flow
            }
            
            // Takımı sil
            val result = repository.deleteTeam(teamId)
            
            if (result) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(message = "Takım silinirken bir hata oluştu."))
            }
        } catch (e: IOException) {
            emit(Resource.Error(message = "Ağ hatası: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Beklenmeyen bir hata oluştu: ${e.localizedMessage}"))
        }
    }
} 