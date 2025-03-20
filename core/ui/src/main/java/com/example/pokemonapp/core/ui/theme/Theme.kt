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

// Pokémon uygulaması için özel renkler
val PokemonRed = Color(0xFFFA5C5C)
val PokemonRedDark = Color(0xFFE63E3E)
val PokemonYellow = Color(0xFFFFD53E)
val PokemonBlue = Color(0xFF52A9FF)
val PokemonGreen = Color(0xFF8CD36A)

private val LightColorScheme = lightColorScheme(
    primary = PokemonRed,
    onPrimary = Color.White,
    primaryContainer = PokemonRed.copy(alpha = 0.8f),
    onPrimaryContainer = Color.White,
    
    secondary = PokemonYellow,
    onSecondary = Color.Black,
    secondaryContainer = PokemonYellow.copy(alpha = 0.7f),
    onSecondaryContainer = Color.Black,
    
    tertiary = PokemonBlue,
    onTertiary = Color.White,
    tertiaryContainer = PokemonBlue.copy(alpha = 0.7f),
    onTertiaryContainer = Color.White,
    
    background = Color.White,
    onBackground = Color(0xFF1A1C1E),
    
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    
    surfaceVariant = Color(0xFFF3F3F3),
    onSurfaceVariant = Color(0xFF43474E),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    
    outline = Color(0xFF73777F)
)

private val DarkColorScheme = darkColorScheme(
    primary = PokemonRed,
    onPrimary = Color.White,
    primaryContainer = PokemonRedDark,
    onPrimaryContainer = Color.White,
    
    secondary = PokemonYellow,
    onSecondary = Color.Black,
    secondaryContainer = PokemonYellow.copy(alpha = 0.5f),
    onSecondaryContainer = Color.Black,
    
    tertiary = PokemonBlue,
    onTertiary = Color.White,
    tertiaryContainer = PokemonBlue.copy(alpha = 0.5f),
    onTertiaryContainer = Color.White,
    
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    
    surface = Color(0xFF111315),
    onSurface = Color(0xFFE2E2E6),
    
    surfaceVariant = Color(0xFF222528),
    onSurfaceVariant = Color(0xFFC3C7CF),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    
    outline = Color(0xFF8D9199)
)

@Composable
fun PokemonAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dinamik renk, Android 12+ için bir özelliktir
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
} 