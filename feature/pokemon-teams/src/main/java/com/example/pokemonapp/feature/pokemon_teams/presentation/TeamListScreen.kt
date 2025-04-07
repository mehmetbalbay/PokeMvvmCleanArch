package com.example.pokemonapp.feature.pokemon_teams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pokemonapp.core.ui.components.EmptyStateContent
import com.example.pokemonapp.core.ui.components.FullScreenEmptyState
import com.example.pokemonapp.core.ui.components.FullScreenLoading
import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam

/**
 * Takım listesi ekranı için ana giriş noktası
 */
@Composable
fun TeamListRoute(
    onBackClick: () -> Unit,
    onTeamClick: (String) -> Unit,
    viewModel: TeamListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    TeamListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTeamClick = onTeamClick,
        onCreateTeamClick = viewModel::showCreateTeamDialog,
        onCreateTeam = viewModel::createTeam,
        onDismissCreateDialog = viewModel::hideCreateTeamDialog,
        onRefresh = viewModel::loadTeams
    )
}

/**
 * Takım listesi ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListScreen(
    uiState: TeamListUiState,
    onBackClick: () -> Unit,
    onTeamClick: (String) -> Unit,
    onCreateTeamClick: () -> Unit,
    onCreateTeam: (String, String) -> Unit,
    onDismissCreateDialog: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokemon Takımlarım") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTeamClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Takım Ekle",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TeamListUiState.Loading -> {
                    FullScreenLoading(message = "Takımlar yükleniyor...")
                }
                
                is TeamListUiState.Success -> {
                    if (uiState.teams.isEmpty()) {
                        FullScreenEmptyState(
                            message = "Henüz bir Pokemon takımınız yok. Yeni takımlar oluşturmak için + butonuna tıklayın.",
                            onAction = onCreateTeamClick,
                            actionText = "Takım Oluştur"
                        )
                    } else {
                        TeamList(
                            teams = uiState.teams,
                            onTeamClick = onTeamClick
                        )
                    }
                }
                
                is TeamListUiState.Error -> {
                    FullScreenEmptyState(
                        message = "Hata: ${uiState.message}",
                        onAction = onRefresh,
                        actionText = "Tekrar Dene"
                    )
                }
            }
            
            if (uiState is TeamListUiState.Success && uiState.showCreateDialog) {
                CreateTeamDialog(
                    onDismiss = onDismissCreateDialog,
                    onCreateTeam = onCreateTeam
                )
            }
        }
    }
}

/**
 * Takım listesi içeriği
 */
@Composable
fun TeamList(
    teams: List<PokemonTeam>,
    onTeamClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(teams) { team ->
            TeamCard(team = team, onClick = { onTeamClick(team.id) })
        }
        
        // Alt boşluk ekleniyor (FAB için)
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Takım kartı
 */
@Composable
fun TeamCard(
    team: PokemonTeam,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (team.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = team.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = "${team.pokemons.size}/6",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (team.pokemons.size == 6) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (team.pokemons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    team.pokemons.take(6).forEach { pokemon ->
                        TeamPokemonAvatar(
                            name = pokemon.name,
                            type = pokemon.types.firstOrNull() ?: "normal",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Boş slotlar için yer tutucu
                    repeat(6 - team.pokemons.size) {
                        EmptyPokemonSlot(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Takımdaki Pokemon avatarı
 */
@Composable
fun TeamPokemonAvatar(
    name: String,
    type: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(getPokemonTypeColor(type).copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.first().toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = getPokemonTypeColor(type)
        )
    }
}

/**
 * Boş Pokemon slotu
 */
@Composable
fun EmptyPokemonSlot(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    )
}

/**
 * Takım oluşturma diyaloğu
 */
@Composable
fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onCreateTeam: (String, String) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var teamDescription by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Takım Oluştur") },
        text = {
            Column {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Takım Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = teamDescription,
                    onValueChange = { teamDescription = it },
                    label = { Text("Açıklama (İsteğe Bağlı)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { 
                            focusManager.clearFocus()
                            if (teamName.isNotBlank()) {
                                onCreateTeam(teamName, teamDescription)
                            }
                        }
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (teamName.isNotBlank()) {
                        onCreateTeam(teamName, teamDescription) 
                    }
                },
                enabled = teamName.isNotBlank()
            ) {
                Text("Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

/**
 * Pokemon tipine göre renk seçimi
 */
@Composable
fun getPokemonTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "fire" -> Color(0xFFEE8130)
        "water" -> Color(0xFF6390F0)
        "electric" -> Color(0xFFF7D02C)
        "grass" -> Color(0xFF7AC74C)
        "ice" -> Color(0xFF96D9D6)
        "fighting" -> Color(0xFFC22E28)
        "poison" -> Color(0xFFA33EA1)
        "ground" -> Color(0xFFE2BF65)
        "flying" -> Color(0xFFA98FF3)
        "psychic" -> Color(0xFFF95587)
        "bug" -> Color(0xFFA6B91A)
        "rock" -> Color(0xFFB6A136)
        "ghost" -> Color(0xFF735797)
        "dragon" -> Color(0xFF6F35FC)
        "dark" -> Color(0xFF705746)
        "steel" -> Color(0xFFB7B7CE)
        "fairy" -> Color(0xFFD685AD)
        else -> MaterialTheme.colorScheme.primary
    }
} 