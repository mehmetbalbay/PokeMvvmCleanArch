package com.example.pokemonapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokemonapp.feature.pokemon_detail.presentation.PokemonDetailScreen
import com.example.pokemonapp.feature.pokemon_list.presentation.PokemonListScreen
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

@Composable
fun PokemonAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "pokemon_list"
    ) {
        composable("pokemon_list") {
            PokemonListScreen(
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
            PokemonDetailScreen(
                navController = navController
            )
        }
    }
} 