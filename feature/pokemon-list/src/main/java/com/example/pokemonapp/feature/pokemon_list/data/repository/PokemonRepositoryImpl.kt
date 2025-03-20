package com.example.pokemonapp.feature.pokemon_list.data.repository

import com.example.pokemonapp.feature.pokemon_list.data.local.PokemonLocalDataSource
import com.example.pokemonapp.feature.pokemon_list.data.remote.PokemonRemoteDataSource
import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonListResult
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PokemonRepository arayüzünün implementasyonu.
 * Uzak ve yerel veri kaynaklarını kullanarak Pokemon verilerini yönetir.
 */
@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonLocalDataSource
) : PokemonRepository {

    private val pokemonTypes = mutableMapOf<Int, List<String>>()

    override suspend fun getPokemons(offset: Int, limit: Int): List<Pokemon> = coroutineScope {
        val pokemonList = remoteDataSource.getPokemons(offset, limit).results
        val deferredTypes = List(pokemonList.size) { index ->
            async {
                try {
                    val id = offset + index + 1
                    val pokemonDetail = remoteDataSource.getPokemonDetail(id)
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
                isFavorite = localDataSource.isFavorite(id)
            )
        }
    }

    override suspend fun getPokemonsWithCount(offset: Int, limit: Int): PokemonListResult = coroutineScope {
        try {
            val response = remoteDataSource.getPokemons(offset = offset, limit = limit)
            
            val pokemonList = response.results
            val deferredTypes = List(pokemonList.size) { index ->
                async {
                    try {
                        val id = offset + index + 1
                        val pokemonDetail = remoteDataSource.getPokemonDetail(id)
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
                    isFavorite = localDataSource.isFavorite(id)
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
        val pokemonDetail = remoteDataSource.getPokemonDetail(id)
        return Pokemon(
            id = id,
            name = pokemonDetail.name.replaceFirstChar { it.uppercase() },
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
            types = pokemonDetail.types.map { it.type.name },
            isFavorite = localDataSource.isFavorite(id)
        )
    }

    override suspend fun toggleFavorite(id: Int): Boolean {
        val isFavorite = localDataSource.isFavorite(id)
        if (isFavorite) {
            localDataSource.removeFromFavorites(id)
            return false
        } else {
            localDataSource.addToFavorites(id)
            return true
        }
    }

    override suspend fun isFavorite(id: Int): Boolean {
        return localDataSource.isFavorite(id)
    }
    
    override fun observeFavorite(id: Int): Flow<Boolean> {
        return localDataSource.observeFavorite(id)
    }
    
    override fun observeAllFavorites(): Flow<List<Int>> {
        return localDataSource.observeAllFavorites()
    }

    /**
     * Pokemon tiplerinin listesini döndürür
     */
    override suspend fun getAllPokemonTypes(): List<String> {
        // API'den tiplerini alma işlemi burada olacak
        
        // Örnek tip listesi
        return listOf(
            "bug", "dark", "dragon", "electric", "fairy", "fighting",
            "fire", "flying", "ghost", "grass", "ground", "ice",
            "normal", "poison", "psychic", "rock", "steel", "water"
        )
    }

    /**
     * Belirtilen tiplere sahip Pokemonları getirir
     */
    override suspend fun getPokemonsByTypes(types: List<String>, offset: Int, limit: Int): List<Pokemon> {
        // Tüm pokemonları alıp, tiplere göre filtreleyerek döndür
        return getPokemons(0, 1000)
            .filter { pokemon -> pokemon.types.any { it in types } }
            .drop(offset)
            .take(limit)
    }

    /**
     * Pokemon detayını getirir
     */
    override suspend fun getPokemonDetail(id: Int): Pokemon {
        val pokemonDetail = remoteDataSource.getPokemonDetail(id)
        
        val types = pokemonDetail.types.map { it.type.name }
        pokemonTypes[id] = types
        
        return Pokemon(
            id = id,
            name = pokemonDetail.name.replaceFirstChar { it.uppercase() },
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
            types = types,
            isFavorite = localDataSource.isFavorite(id),
            height = pokemonDetail.height,
            weight = pokemonDetail.weight,
            stats = pokemonDetail.stats.associate { 
                it.stat.name to it.baseStat 
            },
            abilities = pokemonDetail.abilities.map { it.ability.name }
        )
    }

    /**
     * Favori pokemonların id listesini getirir
     */
    override suspend fun getFavoritePokemons(): List<Int> {
        return localDataSource.getFavorites()
    }
} 