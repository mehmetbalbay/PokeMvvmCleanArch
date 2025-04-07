package com.example.pokemonapp.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pokemonapp.core.ui.theme.PokemonStatsBarShape

@Composable
fun StatBar(
    statName: String,
    statValue: Int,
    maxValue: Int = 255,
    color: Color,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000
) {
    var startAnimation by remember { mutableStateOf(false) }
    val statPercentage = statValue.toFloat() / maxValue
    val animatedPercentage by animateFloatAsState(
        targetValue = if (startAnimation) statPercentage else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = LinearOutSlowInEasing
        ),
        label = "StatPercentage"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stat Name
        Text(
            text = statName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp)
        )
        
        // Stat Value
        Text(
            text = statValue.toString(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp),
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Progress Bar Background
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(PokemonStatsBarShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Progress Bar Fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .animateContentSize()
                    .fillMaxWidth(animatedPercentage)
                    .clip(PokemonStatsBarShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun StatsSection(
    stats: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val statColors = mapOf(
        "hp" to Color(0xFFFF5959),
        "attack" to Color(0xFFF5AC78),
        "defense" to Color(0xFFFAE078),
        "special-attack" to Color(0xFF9DB7F5),
        "special-defense" to Color(0xFFA7DB8D),
        "speed" to Color(0xFFFA92B2)
    )
    
    Column(modifier = modifier) {
        stats.forEach { (statName, statValue) ->
            val displayName = when(statName) {
                "hp" -> "HP"
                "attack" -> "Atak"
                "defense" -> "Savunma"
                "special-attack" -> "Öz. Atak"
                "special-defense" -> "Öz. Savunma"
                "speed" -> "Hız"
                else -> statName
            }
            
            val color = statColors[statName] ?: MaterialTheme.colorScheme.primary
            
            StatBar(
                statName = displayName,
                statValue = statValue,
                color = color,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatBarPreview() {
    StatBar(
        statName = "HP",
        statValue = 80,
        color = Color(0xFFFF5959)
    )
}

@Preview(showBackground = true)
@Composable
fun StatsSectionPreview() {
    val sampleStats = mapOf(
        "hp" to 80,
        "attack" to 95,
        "defense" to 60,
        "special-attack" to 110,
        "special-defense" to 70,
        "speed" to 100
    )
    
    StatsSection(
        stats = sampleStats,
        modifier = Modifier.padding(16.dp)
    )
} 