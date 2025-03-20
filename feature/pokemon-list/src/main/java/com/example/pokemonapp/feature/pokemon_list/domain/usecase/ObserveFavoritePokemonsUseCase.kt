package com.example.pokemonapp.feature.pokemon_list.domain.usecase

import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Tüm favori Pokemon ID'lerini gözlemlemek için kullanılan use case.
 */
class ObserveFavoritePokemonsUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    /**
     * Favori Pokemon ID'lerini gözlemler ve değişiklikler oldukça bildirir.
     * @return Favori Pokemon ID'lerini içeren Flow<List<Int>>
     */
    operator fun invoke(): Flow<List<Int>> {
        return repository.observeAllFavorites()
    }
} 