package com.example.pokemonapp.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Takımdaki Pokemon'ları temsil eden tablo.
 * Foreign key ile takım tablosuna bağlıdır.
 */
@Entity(
    tableName = "team_pokemons",
    primaryKeys = ["team_id", "pokemon_id"],
    foreignKeys = [
        ForeignKey(
            entity = PokemonTeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("team_id")
    ]
)
data class TeamPokemonEntity(
    @ColumnInfo(name = "team_id")
    val teamId: String,
    
    @ColumnInfo(name = "pokemon_id")
    val pokemonId: Int,
    
    val name: String,
    
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    
    // JSON dizisi olarak saklanacak
    val types: String,
    
    // Takım içindeki sıralama
    val order: Int = 0
) 