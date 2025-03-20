package com.example.pokemonapp.feature.pokemon_list.domain.usecase

import com.example.pokemonapp.core.common.Resource
import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

/**
 * Pokemon detaylarını ID'ye göre getiren use case.
 */
class GetPokemonDetailUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    /**
     * Belirtilen ID'ye sahip Pokemon'un detaylarını getirir.
     * 
     * @param id Pokemon ID'si
     * @return Pokemon detaylarını içeren Resource<Pokemon> türünde Flow
     */
    operator fun invoke(id: Int): Flow<Resource<Pokemon>> = flow {
        try {
            emit(Resource.Loading())
            
            val pokemon = repository.getPokemonById(id)
            emit(Resource.Success(pokemon))
            
        } catch (e: IOException) {
            emit(Resource.Error("Ağ hatası: Lütfen internet bağlantınızı kontrol edin."))
        } catch (e: Exception) {
            emit(Resource.Error("Bir hata oluştu: ${e.message ?: "Bilinmeyen bir hata"}"))
        }
    }
} 