package com.example.pokemonapp.feature.pokemon_teams.domain.model

import java.util.UUID

/**
 * Takımdaki bir Pokemon'u temsil eden model
 */
data class TeamPokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val order: Int, // Takımdaki sırası
    val pokemonId: String = id.toString(), // Pokemon ID'si (string formatında)
    val level: Int = 50, // Varsayılan seviye
    val attack: Int = 70, // Varsayılan saldırı
    val defense: Int = 70, // Varsayılan savunma
    val speed: Int = 70 // Varsayılan hız
) 