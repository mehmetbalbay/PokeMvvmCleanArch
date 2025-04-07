package com.example.pokemonapp.feature.pokemon_list.data.remote

import com.example.pokemonapp.feature.pokemon_list.data.remote.dto.PokemonDetailDto
import com.example.pokemonapp.feature.pokemon_list.data.remote.dto.PokemonResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PokemonRemoteDataSource arayüzünün Retrofit kullanarak implementasyonu.
 */
@Singleton
class PokemonRemoteDataSourceImpl @Inject constructor(
    private val api: PokemonApi
) : PokemonRemoteDataSource {
    
    override suspend fun getPokemons(offset: Int, limit: Int): PokemonResponse {
        println("API'ye istek gönderiliyor: offset=$offset, limit=$limit")
        try {
            val response = api.getPokemons(offset, limit)
            println("API yanıtı başarılı: ${response.results.size} Pokemon, toplam: ${response.count}")
            println("Sonraki sayfa: ${response.next}")
            return response
        } catch (e: Exception) {
            println("API hatası: ${e.message}")
            throw e
        }
    }
    
    override suspend fun getPokemonDetail(id: Int): PokemonDetailDto {
        println("Pokemon detayı isteniyor: id=$id")
        try {
            val detail = api.getPokemonDetail(id)
            println("Pokemon detayı alındı: ${detail.name}")
            return detail
        } catch (e: Exception) {
            println("Pokemon detayı alınamadı: ${e.message}")
            throw e
        }
    }
} 