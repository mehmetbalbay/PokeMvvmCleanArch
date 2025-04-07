package com.example.pokemonapp.feature.pokemon_detail.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pokemonapp.core.ui.components.PokemonTypeChip
import com.example.pokemonapp.core.ui.components.StatsSection
import com.example.pokemonapp.core.ui.theme.PokemonDetailCardShape
import com.example.pokemonapp.core.ui.theme.getPokemonTypeColor
import com.example.pokemonapp.feature.pokemon_detail.domain.model.PokemonDetail
import com.example.pokemonapp.feature.pokemon_detail.domain.model.Stat
import com.example.pokemonapp.feature.pokemon_detail.presentation.PokemonDetailUiState.*

@Composable
fun PokemonDetailRoute(
    onBackClick: () -> Unit,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    PokemonDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::loadPokemonDetail,
        onToggleFavorite = viewModel::toggleFavorite
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Favori işlemi mesajı
    LaunchedEffect(uiState) {
        if (uiState is Success && uiState.favoriteActionMessage != null) {
            snackbarHostState.showSnackbar(
                message = uiState.favoriteActionMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (uiState) {
                is Loading -> {
                    LoadingState()
                }
                is Error -> {
                    ErrorState(
                        message = uiState.message,
                        onRetry = onRetry
                    )
                }
                is Success -> {
                    val pokemon = uiState.pokemon
                    val dominantType = pokemon.types.firstOrNull() ?: "normal"
                    val typeColor = getPokemonTypeColor(dominantType)
                    val gradientBackground = Brush.verticalGradient(
                        colors = listOf(
                            typeColor,
                            typeColor.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Arka plan gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(gradientBackground)
                        )
                        
                        // İçerik
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Başlık çubuğu
                            TopAppBar(
                                title = {},
                                navigationIcon = {
                                    IconButton(
                                        onClick = onBackClick,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Geri",
                                            tint = Color.Black
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = onToggleFavorite,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.5f))
                                            .size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (pokemon.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (pokemon.isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                                            tint = if (pokemon.isFavorite) Color.Red else Color.Black
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    navigationIconContentColor = Color.White
                                )
                            )
                            
                            // Pokemon görüntüsü ve bilgileri
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                // Pokemon görüntüsü
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(pokemon.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = pokemon.name,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .offset(y = (-40).dp),
                                    contentScale = ContentScale.Fit
                                )
                                
                                // Detaylar
                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 140.dp),
                                    shape = PokemonDetailCardShape,
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp)
                                            .verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Pokemon numarası ve adı
                                        Text(
                                            text = "#${pokemon.id.toString().padStart(3, '0')}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        
                                        Text(
                                            text = pokemon.name.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        // Pokemon tipleri
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(bottom = 24.dp)
                                        ) {
                                            pokemon.types.forEach { type ->
                                                PokemonTypeChip(
                                                    type = type,
                                                    modifier = Modifier.height(32.dp)
                                                )
                                            }
                                        }
                                        
                                        // Fiziksel bilgiler
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            PhysicalInfo(
                                                title = "Boy",
                                                value = "${pokemon.height/10.0} m"
                                            )
                                            
                                            VerticalDivider()
                                            
                                            PhysicalInfo(
                                                title = "Ağırlık",
                                                value = "${pokemon.weight/10.0} kg"
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        // Yetenekler Başlığı
                                        SectionTitle(title = "Yetenekler")
                                        
                                        // Yetenekler
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            pokemon.abilities.forEach { ability ->
                                                val displayName = ability.replaceFirstChar { it.uppercase() }
                                                    .replace("-", " ")
                                                
                                                AbilityChip(
                                                    name = displayName,
                                                    color = typeColor,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        // İstatistikler Başlığı
                                        SectionTitle(title = "İstatistikler")
                                        
                                        // İstatistikler
                                        StatsSection(
                                            stats = pokemon.stats.associate { it.name to it.value },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp, bottom = 24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    )
}

@Composable
fun PhysicalInfo(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AbilityChip(
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Pokemon yükleniyor...",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bir hata oluştu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Tekrar Dene")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SectionTitlePreview() {
    SectionTitle(title = "Özellikler")
}

@Preview(showBackground = true)
@Composable
fun PhysicalInfoPreview() {
    PhysicalInfo(
        title = "Ağırlık",
        value = "6.9kg"
    )
}

@Preview(showBackground = true)
@Composable
fun AbilityChipPreview() {
    AbilityChip(
        name = "Overgrow",
        color = getPokemonTypeColor("grass")
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PokemonDetailScreenPreview() {
    val samplePokemon = PokemonDetailUiState.Success(
        pokemon = PokemonDetail(
            id = 1,
            name = "bulbasaur",
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            types = listOf("grass", "poison"),
            height = 7,
            weight = 69,
            abilities = listOf("overgrow", "chlorophyll"),
            stats = listOf(
                Stat("hp", 45),
                Stat("attack", 49),
                Stat("defense", 49),
                Stat("special-attack", 65),
                Stat("special-defense", 65),
                Stat("speed", 45)
            ),
            isFavorite = true
        ),
        favoriteActionMessage = null
    )
    
    PokemonDetailScreen(
        uiState = samplePokemon,
        onBackClick = {},
        onRetry = {},
        onToggleFavorite = {}
    )
} 