package com.example.pokemonapp.feature.pokemon_detail.data.repository

import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.core.database.entities.FavoritePokemonEntity
import com.example.pokemonapp.feature.pokemon_detail.data.remote.PokemonDetailApi
import com.example.pokemonapp.feature.pokemon_detail.domain.model.Ability
import com.example.pokemonapp.feature.pokemon_detail.domain.model.PokemonDetail
import com.example.pokemonapp.feature.pokemon_detail.domain.model.Stat
import com.example.pokemonapp.feature.pokemon_detail.domain.repository.PokemonDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PokemonDetailRepositoryImpl @Inject constructor(
    private val api: PokemonDetailApi,
    private val favoriteDao: FavoritePokemonDao
) : PokemonDetailRepository {

    override suspend fun getPokemonDetail(id: Int): PokemonDetail {
        val response = api.getPokemonDetail(id)
        val isFavorite = favoriteDao.isFavorite(response.id)
        
        return PokemonDetail(
            id = response.id,
            name = response.name.replaceFirstChar { it.uppercase() },
            imageUrl = response.sprites.front_default,
            types = response.types.map { it.type.name },
            height = response.height,
            weight = response.weight,
            stats = response.stats.map { Stat(it.stat.name, it.base_stat) },
            abilities = response.abilities.map { Ability(it.ability.name, it.is_hidden) },
            isFavorite = isFavorite
        )
    }

    override suspend fun toggleFavorite(id: Int): Boolean {
        val isFavorite = favoriteDao.isFavorite(id)
        if (isFavorite) {
            favoriteDao.removeFromFavorites(id)
            return false
        } else {
            favoriteDao.addToFavorites(FavoritePokemonEntity(id))
            return true
        }
    }
    
    override fun observeFavorite(id: Int): Flow<Boolean> {
        return favoriteDao.observeFavorite(id)
    }
} 