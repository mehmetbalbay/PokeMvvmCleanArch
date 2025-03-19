package com.example.pokemonapp.feature.pokemon_list.di

import com.example.pokemonapp.core.database.PokemonDatabase
import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.feature.pokemon_list.data.remote.PokemonApi
import com.example.pokemonapp.feature.pokemon_list.data.repository.PokemonRepositoryImpl
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PokemonListModule {

    @Provides
    @Singleton
    fun providePokemonApi(retrofit: Retrofit): PokemonApi {
        return retrofit.create(PokemonApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoritePokemonDao(database: PokemonDatabase): FavoritePokemonDao {
        return database.favoritePokemonDao()
    }

    @Provides
    @Singleton
    fun providePokemonRepository(api: PokemonApi, favoriteDao: FavoritePokemonDao): PokemonRepository {
        return PokemonRepositoryImpl(api, favoriteDao)
    }
} 