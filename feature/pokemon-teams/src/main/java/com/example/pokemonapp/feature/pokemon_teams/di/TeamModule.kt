package com.example.pokemonapp.feature.pokemon_teams.di

import com.example.pokemonapp.core.database.PokemonDatabase
import com.example.pokemonapp.core.database.dao.PokemonTeamDao
import com.example.pokemonapp.feature.pokemon_teams.data.repository.PokemonTeamRepositoryImpl
import com.example.pokemonapp.feature.pokemon_teams.domain.repository.PokemonTeamRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TeamModule {
    
    @Provides
    fun providePokemonTeamDao(database: PokemonDatabase): PokemonTeamDao {
        return database.pokemonTeamDao()
    }
    
    @Provides
    @Singleton
    fun providePokemonTeamRepository(
        teamDao: PokemonTeamDao,
        gson: Gson
    ): PokemonTeamRepository {
        return PokemonTeamRepositoryImpl(teamDao, gson)
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
} 