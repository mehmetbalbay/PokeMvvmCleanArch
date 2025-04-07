package com.example.pokemonapp.feature.pokemon_teams.domain.model

import java.util.UUID

/**
 * Pokemon takımını temsil eden domain model sınıfı.
 * Takım 6 adede kadar Pokemon içerebilir.
 */
data class PokemonTeam(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val pokemons: List<TeamPokemon> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Takımdaki bir Pokemon'u temsil eden model
 */
data class TeamPokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val order: Int // Takımdaki sırası
) 