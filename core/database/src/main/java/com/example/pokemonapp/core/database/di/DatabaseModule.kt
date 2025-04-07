package com.example.pokemonapp.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.pokemonapp.core.database.PokemonDatabase
import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.core.database.dao.PokemonTeamDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePokemonDatabase(@ApplicationContext context: Context): PokemonDatabase {
        return Room.databaseBuilder(
            context,
            PokemonDatabase::class.java,
            "pokemon_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun providePokemonTeamDao(database: PokemonDatabase): PokemonTeamDao {
        return database.pokemonTeamDao()
    }
} 