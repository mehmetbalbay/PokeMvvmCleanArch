package com.example.pokemonapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokemonapp.feature.pokemon_detail.presentation.PokemonDetailRoute
import com.example.pokemonapp.feature.pokemon_list.presentation.PokemonListRoute
import com.example.pokemonapp.ui.theme.PokemonAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokemonAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PokemonAppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonAppNavigation() {
    val navController = rememberNavController()
    
    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = "pokemon_list"
            ) {
                composable("pokemon_list") {
                    PokemonListRoute(
                        onPokemonClick = { pokemonId ->
                            navController.navigate("pokemon_detail/$pokemonId")
                        }
                    )
                }

                composable(
                    route = "pokemon_detail/{pokemonId}",
                    arguments = listOf(
                        navArgument("pokemonId") { type = NavType.IntType }
                    )
                ) {
                    PokemonDetailRoute(
                        onBackClick = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PokemonAppThemePreview() {
    PokemonAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Gerçek navigasyon yerine basit bir içerik kullanıyoruz
            Text(
                text = "Pokemon App Preview",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
} 