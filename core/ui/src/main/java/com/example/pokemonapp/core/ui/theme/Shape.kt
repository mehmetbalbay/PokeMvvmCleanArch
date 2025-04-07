package com.example.pokemonapp.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

// Pokémon kartları için özel şekiller
val PokemonCardShape = RoundedCornerShape(20.dp)
val PokemonTypeChipShape = RoundedCornerShape(50.dp) // Tam yuvarlak chip'ler
val PokemonStatsBarShape = RoundedCornerShape(4.dp)
val PokemonDetailCardShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp) 