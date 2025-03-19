package com.example.pokemonapp.feature.pokemon_detail.di

import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.feature.pokemon_detail.data.remote.PokemonDetailApi
import com.example.pokemonapp.feature.pokemon_detail.data.repository.PokemonDetailRepositoryImpl
import com.example.pokemonapp.feature.pokemon_detail.domain.repository.PokemonDetailRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PokemonDetailModule {

    @Provides
    @Singleton
    fun providePokemonDetailApi(retrofit: Retrofit): PokemonDetailApi {
        return retrofit.create(PokemonDetailApi::class.java)
    }

    @Provides
    @Singleton
    fun providePokemonDetailRepository(
        api: PokemonDetailApi,
        favoriteDao: FavoritePokemonDao
    ): PokemonDetailRepository {
        return PokemonDetailRepositoryImpl(api, favoriteDao)
    }
} 