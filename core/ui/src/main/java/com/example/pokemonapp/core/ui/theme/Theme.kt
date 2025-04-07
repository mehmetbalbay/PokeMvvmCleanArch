package com.example.pokemonapp.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Pokemon kart tiplerine göre renk paleti
val PokemonTypeColors = mapOf(
    "normal" to Color(0xFFA8A878),
    "fire" to Color(0xFFFB6C6C),
    "water" to Color(0xFF76BDFE),
    "electric" to Color(0xFFFFD970),
    "grass" to Color(0xFF48D0B0),
    "ice" to Color(0xFF98D8D8),
    "fighting" to Color(0xFFC03028),
    "poison" to Color(0xFFA040A0),
    "ground" to Color(0xFFE0C068),
    "flying" to Color(0xFFA890F0),
    "psychic" to Color(0xFFF85888),
    "bug" to Color(0xFFA8B820),
    "rock" to Color(0xFFB8A038),
    "ghost" to Color(0xFF705898),
    "dragon" to Color(0xFF7038F8),
    "dark" to Color(0xFF705848),
    "steel" to Color(0xFFB8B8D0),
    "fairy" to Color(0xFFEE99AC)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF48D0B0),      // Bulbasaur rengi
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A9D8F),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFB6C6C),    // Charmander rengi
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE76F51),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF76BDFE),     // Squirtle rengi
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF457B9D),
    onTertiaryContainer = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF48D0B0),      // Bulbasaur rengi
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBEF3E8),
    onPrimaryContainer = Color(0xFF0B5A4F),
    secondary = Color(0xFFFB6C6C),    // Charmander rengi
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = Color(0xFF76BDFE),     // Squirtle rengi
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD3E4FF),
    onTertiaryContainer = Color(0xFF001A41),
    background = Color(0xFFFEF6E6),   // Yumuşak kremsi arka plan
    onBackground = Color(0xFF1B1B1A),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1A),
)

@Composable
fun PokemonAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

// Pokemon tipi için arka plan rengi seçici fonksiyon
@Composable
fun getPokemonTypeColor(type: String): Color {
    return PokemonTypeColors[type.lowercase()] ?: MaterialTheme.colorScheme.primary
} 