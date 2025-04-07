package com.example.pokemonapp.feature.pokemon_teams.data.repository

import com.example.pokemonapp.core.database.dao.PokemonTeamDao
import com.example.pokemonapp.core.database.entities.PokemonTeamEntity
import com.example.pokemonapp.core.database.entities.TeamPokemonEntity
import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam
import com.example.pokemonapp.feature.pokemon_teams.domain.model.TeamPokemon
import com.example.pokemonapp.feature.pokemon_teams.domain.repository.PokemonTeamRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PokemonTeamRepositoryImpl @Inject constructor(
    private val pokemonTeamDao: PokemonTeamDao,
    private val gson: Gson
) : PokemonTeamRepository {

    override fun getAllTeams(): Flow<List<PokemonTeam>> {
        return pokemonTeamDao.getAllTeams().map { entities ->
            entities.map { entity ->
                mapTeamEntityToTeam(entity)
            }
        }
    }

    override suspend fun getTeamById(teamId: String): PokemonTeam? {
        val teamEntity = pokemonTeamDao.getTeamById(teamId) ?: return null
        val teamPokemons = pokemonTeamDao.getPokemonsForTeamSync(teamId)
        return mapTeamEntityToTeam(teamEntity, teamPokemons)
    }
    
    override suspend fun getTeam(teamId: String): PokemonTeam? {
        return getTeamById(teamId)
    }

    override fun getTeamPokemons(teamId: String): Flow<List<TeamPokemon>> {
        return pokemonTeamDao.getPokemonsForTeam(teamId).map { entities ->
            entities.map { entity ->
                mapTeamPokemonEntityToTeamPokemon(entity)
            }
        }
    }

    override suspend fun createTeam(team: PokemonTeam): String {
        val teamEntity = mapTeamToTeamEntity(team)
        pokemonTeamDao.insertTeam(teamEntity)
        
        // Takıma Pokemon'ları ekle
        if (team.pokemons.isNotEmpty()) {
            val pokemonEntities = team.pokemons.map { pokemon ->
                mapTeamPokemonToTeamPokemonEntity(teamEntity.id, pokemon)
            }
            pokemonTeamDao.insertTeamPokemons(pokemonEntities)
        }
        
        return teamEntity.id
    }

    override suspend fun updateTeam(team: PokemonTeam) {
        val teamEntity = mapTeamToTeamEntity(team)
        val pokemonEntities = team.pokemons.map { pokemon ->
            mapTeamPokemonToTeamPokemonEntity(teamEntity.id, pokemon)
        }
        pokemonTeamDao.updateTeamWithPokemons(teamEntity, pokemonEntities)
    }

    override suspend fun deleteTeam(teamId: String): Boolean {
        return try {
            pokemonTeamDao.deleteTeamById(teamId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addPokemonToTeam(teamId: String, pokemon: TeamPokemon) {
        val entity = mapTeamPokemonToTeamPokemonEntity(teamId, pokemon)
        pokemonTeamDao.insertTeamPokemon(entity)
    }

    override suspend fun removePokemonFromTeam(teamId: String, pokemonId: Int) {
        pokemonTeamDao.removeTeamPokemon(teamId, pokemonId)
    }
    
    override suspend fun removePokemonFromTeam(teamId: String, pokemonId: String): Boolean {
        return try {
            pokemonTeamDao.removeTeamPokemon(teamId, pokemonId.toIntOrNull() ?: return false)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun reorderTeamPokemons(teamId: String, pokemonIds: List<Int>) {
        val pokemons = pokemonTeamDao.getPokemonsForTeamSync(teamId)
        val updatedPokemons = pokemons.map { entity ->
            val newOrder = pokemonIds.indexOf(entity.pokemonId)
            if (newOrder >= 0) {
                entity.copy(order = newOrder)
            } else {
                entity
            }
        }
        pokemonTeamDao.insertTeamPokemons(updatedPokemons)
    }

    override suspend fun getTeamSize(teamId: String): Int {
        return pokemonTeamDao.getTeamSize(teamId)
    }

    // Yardımcı dönüştürme metotları
    private suspend fun mapTeamEntityToTeam(
        entity: PokemonTeamEntity, 
        pokemonEntities: List<TeamPokemonEntity>? = null
    ): PokemonTeam {
        val pokemons = pokemonEntities 
            ?: pokemonTeamDao.getPokemonsForTeamSync(entity.id)
        
        return PokemonTeam(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            pokemons = pokemons.map { mapTeamPokemonEntityToTeamPokemon(it) },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    private fun mapTeamToTeamEntity(team: PokemonTeam): PokemonTeamEntity {
        return PokemonTeamEntity(
            id = team.id,
            name = team.name,
            description = team.description,
            createdAt = team.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private fun mapTeamPokemonEntityToTeamPokemon(entity: TeamPokemonEntity): TeamPokemon {
        val typesList = gson.fromJson<List<String>>(
            entity.types,
            object : TypeToken<List<String>>() {}.type
        ) ?: emptyList()
        
        return TeamPokemon(
            id = entity.pokemonId,
            pokemonId = entity.pokemonId.toString(),
            name = entity.name,
            imageUrl = entity.imageUrl,
            types = typesList,
            order = entity.order
        )
    }
    
    private fun mapTeamPokemonToTeamPokemonEntity(teamId: String, pokemon: TeamPokemon): TeamPokemonEntity {
        val typesJson = gson.toJson(pokemon.types)
        
        return TeamPokemonEntity(
            teamId = teamId,
            pokemonId = pokemon.id,
            name = pokemon.name,
            imageUrl = pokemon.imageUrl,
            types = typesJson,
            order = pokemon.order
        )
    }
} 