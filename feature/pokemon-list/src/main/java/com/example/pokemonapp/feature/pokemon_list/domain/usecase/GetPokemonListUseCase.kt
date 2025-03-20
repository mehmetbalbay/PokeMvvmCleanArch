package com.example.pokemonapp.feature.pokemon_list.domain.usecase

import com.example.pokemonapp.core.common.Resource
import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonListResult
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

/**
 * Pokemon listesini getirmek için kullanılan use case.
 * Paging parametrelerini destekler ve sayfalandırma için gerekli bilgileri döndürür.
 */
class GetPokemonListUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    /**
     * Belirtilen offset ve limit parametrelerine göre Pokemon listesini döndürür.
     * 
     * @param offset Listeyi başlatmak için başlangıç noktası
     * @param limit Getirilecek Pokemon sayısı
     * @return Pokemon listesi ve toplam kayıt sayısını içeren Resource<PokemonListResult> türünde Flow
     */
    operator fun invoke(offset: Int = 0, limit: Int = 20): Flow<Resource<PokemonListResult>> = flow {
        try {
            emit(Resource.Loading())
            
            val pokemonListResult = repository.getPokemonsWithCount(offset, limit)
            
            if (pokemonListResult.pokemons.isEmpty()) {
                emit(Resource.Empty("Pokemon bulunamadı"))
            } else {
                emit(Resource.Success(pokemonListResult))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Ağ hatası: Lütfen internet bağlantınızı kontrol edin."))
        } catch (e: Exception) {
            emit(Resource.Error("Bir hata oluştu: ${e.message ?: "Bilinmeyen bir hata"}"))
        }
    }
} 