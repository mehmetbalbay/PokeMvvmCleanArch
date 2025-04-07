package com.example.pokemonapp.feature.pokemon_list.di

import com.example.pokemonapp.core.database.PokemonDatabase
import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.feature.pokemon_list.data.local.PokemonLocalDataSource
import com.example.pokemonapp.feature.pokemon_list.data.local.PokemonLocalDataSourceImpl
import com.example.pokemonapp.feature.pokemon_list.data.remote.PokemonApi
import com.example.pokemonapp.feature.pokemon_list.data.remote.PokemonRemoteDataSource
import com.example.pokemonapp.feature.pokemon_list.data.remote.PokemonRemoteDataSourceImpl
import com.example.pokemonapp.feature.pokemon_list.data.repository.PokemonRepositoryImpl
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Pokemon listesi modülü için DI konfigürasyonu.
 * Retrofit ve Database ile ilgili bağımlılıkları sağlar.
 */
@Module
@InstallIn(SingletonComponent::class)
object PokemonListDataModule {

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
}

/**
 * Pokemon listesi modülü için binding DI konfigürasyonu.
 * Repository ve veri kaynaklarının uygulamalarını sağlar.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PokemonListBindModule {

    @Binds
    @Singleton
    abstract fun bindPokemonRepository(
        repositoryImpl: PokemonRepositoryImpl
    ): PokemonRepository
    
    @Binds
    @Singleton
    abstract fun bindPokemonLocalDataSource(
        localDataSourceImpl: PokemonLocalDataSourceImpl
    ): PokemonLocalDataSource
    
    @Binds
    @Singleton
    abstract fun bindPokemonRemoteDataSource(
        remoteDataSourceImpl: PokemonRemoteDataSourceImpl
    ): PokemonRemoteDataSource
} 