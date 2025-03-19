package com.example.pokemonapp.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_pokemons")
data class FavoritePokemonEntity(
    @PrimaryKey
    val pokemonId: Int
)