package com.example.pokemonapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pokemonapp.core.database.dao.FavoritePokemonDao
import com.example.pokemonapp.core.database.entities.FavoritePokemonEntity

@Database(
    entities = [FavoritePokemonEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun favoritePokemonDao(): FavoritePokemonDao
} 