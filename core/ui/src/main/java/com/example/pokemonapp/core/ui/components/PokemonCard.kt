package com.example.pokemonapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pokemonapp.core.ui.model.PokemonItemUiModel
import com.example.pokemonapp.core.ui.theme.PokemonCardShape
import com.example.pokemonapp.core.ui.theme.PokemonTypeChipShape
import com.example.pokemonapp.core.ui.theme.getPokemonTypeColor

@Composable
fun PokemonCard(
    pokemon: PokemonItemUiModel,
    onClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dominantType = pokemon.types.firstOrNull() ?: "normal"
    val typeColor = getPokemonTypeColor(dominantType)
    val gradient = Brush.verticalGradient(
        colors = listOf(
            typeColor,
            typeColor.copy(alpha = 0.8f)
        )
    )

    Box(
        modifier = modifier
            .height(180.dp)
            .padding(8.dp)
    ) {
        // Kart
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            shape = PokemonCardShape,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
            ) {
                // Pokeball watermark (arka planda)
                AsyncImage(
                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/poke-ball.png",
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 4.dp, end = 4.dp)
                        .offset(x = 30.dp, y = 30.dp)
                        .alpha(0.3f),
                    contentScale = ContentScale.Fit
                )
                
                // İçerik
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Üst kısım: Numara ve favori butonu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pokemon Number
                        Text(
                            text = "#${pokemon.id.toString().padStart(3, '0')}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        // Favori ikonu
                        IconButton(
                            onClick = { onFavoriteClick(pokemon.isFavorite) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (pokemon.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (pokemon.isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                                tint = if (pokemon.isFavorite) Color.Red else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    
                    // Pokemon Name
                    Text(
                        text = pokemon.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    // Types
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pokemon.types.forEach { type ->
                            Box(
                                modifier = Modifier
                                    .clip(PokemonTypeChipShape)
                                    .background(Color.White.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = type.replaceFirstChar { it.uppercase() },
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Pokemon Image
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun PokemonTypeChip(
    type: String,
    modifier: Modifier = Modifier
) {
    val typeColor = getPokemonTypeColor(type)
    
    Box(
        modifier = modifier
            .clip(PokemonTypeChipShape)
            .background(typeColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonCardPreview() {
    val samplePokemon = PokemonItemUiModel(
        id = 1,
        name = "bulbasaur",
        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
        types = listOf("grass", "poison"),
        isFavorite = true
    )
    
    PokemonCard(
        pokemon = samplePokemon,
        onClick = {},
        onFavoriteClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PokemonTypeChipPreview() {
    PokemonTypeChip(type = "fire")
} 