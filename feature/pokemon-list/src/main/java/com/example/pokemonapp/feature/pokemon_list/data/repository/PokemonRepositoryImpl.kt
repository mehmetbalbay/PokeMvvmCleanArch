package com.example.pokemonapp.feature.pokemon_list.data.repository

import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.core.database.entities.FavoritePokemonEntity
import com.example.pokemonapp.feature.pokemon_list.data.remote.PokemonApi
import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonListResult
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokemonApi,
    private val favoriteDao: FavoritePokemonDao
) : PokemonRepository {

    private val pokemonTypes = mutableMapOf<Int, List<String>>()

    override suspend fun getPokemons(offset: Int, limit: Int): List<Pokemon> = coroutineScope {
        val pokemonList = api.getPokemons(offset, limit).results
        val deferredTypes = List(pokemonList.size) { index ->
            async {
                try {
                    val id = offset + index + 1
                    val pokemonDetail = api.getPokemonDetail(id)
                    id to pokemonDetail.types.map { it.type.name }
                } catch (e: Exception) {
                    (offset + index + 1) to listOf("normal")
                }
            }
        }
        deferredTypes.forEach { deferred ->
            val (id, types) = deferred.await()
            pokemonTypes[id] = types
        }

        pokemonList.mapIndexed { index, result ->
            val id = offset + index + 1
            Pokemon(
                id = id,
                name = result.name.replaceFirstChar { it.uppercase() },
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
                types = pokemonTypes[id] ?: listOf("normal"),
                isFavorite = isFavorite(id)
            )
        }
    }

    override suspend fun getPokemonsWithCount(offset: Int, limit: Int): PokemonListResult = coroutineScope {
        try {
            val response = api.getPokemons(offset = offset, limit = limit)
            
            val pokemonList = response.results
            val deferredTypes = List(pokemonList.size) { index ->
                async {
                    try {
                        val id = offset + index + 1
                        val pokemonDetail = api.getPokemonDetail(id)
                        id to pokemonDetail.types.map { it.type.name }
                    } catch (e: Exception) {
                        // Tip bilgisi alınamazsa normal tip olarak varsayalım
                        (offset + index + 1) to listOf("normal")
                    }
                }
            }
            deferredTypes.forEach { deferred ->
                val (id, types) = deferred.await()
                pokemonTypes[id] = types
            }

            val pokemons = pokemonList.mapIndexed { index, result ->
                val id = offset + index + 1
                Pokemon(
                    id = id,
                    name = result.name.replaceFirstChar { it.uppercase() },
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
                    types = pokemonTypes[id] ?: listOf("normal"),
                    isFavorite = isFavorite(id)
                )
            }
            
            return@coroutineScope PokemonListResult(
                pokemons = pokemons,
                count = response.count
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getPokemonById(id: Int): Pokemon {
        val pokemonDetail = api.getPokemonDetail(id)
        return Pokemon(
            id = id,
            name = pokemonDetail.name.replaceFirstChar { it.uppercase() },
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
            types = pokemonDetail.types.map { it.type.name },
            isFavorite = isFavorite(id)
        )
    }

    override suspend fun toggleFavorite(id: Int): Boolean {
        val isFavorite = isFavorite(id)
        if (isFavorite) {
            favoriteDao.removeFromFavorites(id)
            return false
        } else {
            favoriteDao.addToFavorites(FavoritePokemonEntity(id))
            return true
        }
    }

    override suspend fun isFavorite(id: Int): Boolean {
        return favoriteDao.isFavorite(id)
    }
    
    override fun observeFavorite(id: Int): Flow<Boolean> {
        return favoriteDao.observeFavorite(id)
    }
    
    override fun observeAllFavorites(): Flow<List<Int>> {
        return favoriteDao.getAllFavorites().map { favoriteList ->
            favoriteList.map { it.pokemonId }
        }
    }
} 