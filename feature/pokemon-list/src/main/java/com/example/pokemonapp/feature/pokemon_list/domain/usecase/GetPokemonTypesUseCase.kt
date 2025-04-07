package com.example.pokemonapp.feature.pokemon_list.domain.usecase

import com.example.pokemonapp.core.common.Resource
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Pokemon tiplerinin listesini getiren UseCase
 */
class GetPokemonTypesUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    /**
     * Pokemon tiplerini Flow olarak döndürür
     */
    operator fun invoke(): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())
            val types = repository.getAllPokemonTypes()
            if (types.isEmpty()) {
                emit(Resource.Empty())
            } else {
                emit(Resource.Success(types))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Pokemon tipleri yüklenirken bir hata oluştu"))
        }
    }
} 