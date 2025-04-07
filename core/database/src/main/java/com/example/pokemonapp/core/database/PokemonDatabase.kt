package com.example.pokemonapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.core.database.dao.PokemonTeamDao
import com.example.pokemonapp.core.database.entities.FavoritePokemonEntity
import com.example.pokemonapp.core.database.entities.PokemonTeamEntity
import com.example.pokemonapp.core.database.entities.TeamPokemonEntity

@Database(
    entities = [
        FavoritePokemonEntity::class, 
        PokemonTeamEntity::class, 
        TeamPokemonEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun favoritePokemonDao(): FavoritePokemonDao
    abstract fun pokemonTeamDao(): PokemonTeamDao
} 