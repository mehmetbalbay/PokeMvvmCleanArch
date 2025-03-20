package com.example.pokemonapp.feature.pokemon_list.domain.usecase

import com.example.pokemonapp.core.common.Resource
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Pokemon'u favorilere eklemek veya çıkarmak için kullanılan use case.
 */
class ToggleFavoritePokemonUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    /**
     * Belirtilen ID'ye sahip Pokemon'u favorilere ekler veya favorilerden çıkarır.
     * 
     * @param id Pokemon ID'si
     * @return İşlem sonucunu bildiren Resource<Boolean> türünde Flow (true: favorilere eklendi, false: favorilerden çıkarıldı)
     */
    operator fun invoke(id: Int): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            
            val isFavorite = repository.toggleFavorite(id)
            
            if (isFavorite) {
                emit(Resource.Success(true, "Pokemon favorilere eklendi"))
            } else {
                emit(Resource.Success(false, "Pokemon favorilerden kaldırıldı"))
            }
            
        } catch (e: Exception) {
            emit(Resource.Error("Favori durumu güncellenirken bir hata oluştu: ${e.message ?: "Bilinmeyen bir hata"}"))
        }
    }
} 