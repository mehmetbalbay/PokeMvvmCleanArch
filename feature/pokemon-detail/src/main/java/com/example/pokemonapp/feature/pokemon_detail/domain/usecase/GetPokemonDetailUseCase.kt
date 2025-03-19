package com.example.pokemonapp.feature.pokemon_detail.domain.usecase

import com.example.pokemonapp.feature.pokemon_detail.domain.model.PokemonDetail
import com.example.pokemonapp.feature.pokemon_detail.domain.repository.PokemonDetailRepository
import javax.inject.Inject

class GetPokemonDetailUseCase @Inject constructor(
    private val repository: PokemonDetailRepository
) {
    suspend operator fun invoke(id: Int): PokemonDetail {
        return repository.getPokemonDetail(id)
    }
} 