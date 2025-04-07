package com.example.pokemonapp.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Pokemon takımı tablosu
 */
@Entity(tableName = "pokemon_teams")
data class PokemonTeamEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    
    val description: String = "",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) 