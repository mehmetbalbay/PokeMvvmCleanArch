package com.example.pokemonapp.feature.pokemon_teams.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pokemonapp.feature.pokemon_teams.domain.model.PokemonTeam
import com.example.pokemonapp.feature.pokemon_teams.domain.model.TeamPokemon

/**
 * Takım detay ekranı için ana giriş noktası
 */
@Composable
fun TeamDetailRoute(
    teamId: String,
    onBackClick: () -> Unit,
    onAddPokemonClick: (String) -> Unit,
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    // ViewModel'in takım ID'sini ayarla
    LaunchedEffect(teamId) {
        viewModel.loadTeam(teamId)
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    TeamDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddPokemonClick = { onAddPokemonClick(teamId) },
        onRemovePokemon = viewModel::removePokemon,
        onConfirmDelete = viewModel::deleteTeam,
        onDismissDeleteDialog = viewModel::hideDeleteDialog,
        onShowDeleteDialog = viewModel::showDeleteDialog,
        onTeamDeleted = onBackClick // Takım silindiğinde geri dön
    )
}

/**
 * Takım detay ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    uiState: TeamDetailUiState,
    onBackClick: () -> Unit,
    onAddPokemonClick: () -> Unit,
    onRemovePokemon: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    onTeamDeleted: () -> Unit
) {
    // Takım silindiğinde geri dön
    LaunchedEffect(uiState) {
        if (uiState is TeamDetailUiState.Deleted) {
            onTeamDeleted()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (uiState) {
                            is TeamDetailUiState.Success -> uiState.team.name
                            else -> "Takım Detayı"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    if (uiState is TeamDetailUiState.Success) {
                        IconButton(onClick = onShowDeleteDialog) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Takımı Sil"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (uiState is TeamDetailUiState.Success && uiState.team.pokemons.size < 6) {
                FloatingActionButton(
                    onClick = onAddPokemonClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Pokemon Ekle",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TeamDetailUiState.Loading -> {
                    LoadingContent()
                }
                
                is TeamDetailUiState.Success -> {
                    TeamDetail(
                        team = uiState.team,
                        onRemovePokemon = onRemovePokemon,
                        onAddPokemonClick = onAddPokemonClick
                    )
                }
                
                is TeamDetailUiState.Error -> {
                    ErrorContent(
                        message = uiState.message,
                        onRetry = { /* Tekrar yükleme işlemi */ }
                    )
                }
                
                is TeamDetailUiState.Deleted -> {
                    // Bu durumda LaunchedEffect ile geri dönülecek
                }
            }
            
            // Silme onay diyaloğu
            if (uiState is TeamDetailUiState.Success && uiState.showDeleteDialog) {
                DeleteTeamConfirmationDialog(
                    teamName = uiState.team.name,
                    onConfirm = onConfirmDelete,
                    onDismiss = onDismissDeleteDialog
                )
            }
        }
    }
}

/**
 * Takım detay içeriği
 */
@Composable
fun TeamDetail(
    team: PokemonTeam,
    onRemovePokemon: (String) -> Unit,
    onAddPokemonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Takım bilgisi
        item {
            TeamInfoCard(team = team)
        }
        
        // Pokemon listesi başlığı
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Takımdaki Pokemonlar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${team.pokemons.size}/6",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Divider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        
        // Pokemon listesi
        if (team.pokemons.isEmpty()) {
            item {
                EmptyPokemonList(onAddPokemonClick = onAddPokemonClick)
            }
        } else {
            items(team.pokemons) { pokemon ->
                TeamPokemonCard(
                    pokemon = pokemon,
                    onRemove = { onRemovePokemon(pokemon.pokemonId) }
                )
            }
        }
        
        // Takım boş değilse ve 6'dan az Pokemon varsa, yeni Pokemon ekle butonu göster
        if (team.pokemons.isNotEmpty() && team.pokemons.size < 6) {
            item {
                AddPokemonButton(onClick = onAddPokemonClick)
            }
        }
        
        // Alt boşluk
        item {
            Spacer(modifier = Modifier.height(80.dp)) // FAB için yer bırak
        }
    }
}

/**
 * Takım bilgi kartı
 */
@Composable
fun TeamInfoCard(
    team: PokemonTeam,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (team.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = team.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Takım istatistikleri (örnek)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TeamStatItem(
                    icon = Icons.Default.Favorite,
                    label = "Güç",
                    value = calculateTeamPower(team),
                    tint = MaterialTheme.colorScheme.error
                )
                
                TeamStatItem(
                    icon = Icons.Default.Shield,
                    label = "Savunma",
                    value = calculateTeamDefense(team),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                
                TeamStatItem(
                    icon = Icons.Default.Speed,
                    label = "Hız",
                    value = calculateTeamSpeed(team),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * Takım istatistik öğesi
 */
@Composable
fun TeamStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Takım Pokemon kartı
 */
@Composable
fun TeamPokemonCard(
    pokemon: TeamPokemon,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pokemon avatarı
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pokemon.name.first().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "Seviye ${pokemon.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Pokemon istatistikler
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = pokemon.attack.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = pokemon.defense.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Silme butonu
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Pokemonu Çıkar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Boş Pokemon listesi içeriği
 */
@Composable
fun EmptyPokemonList(
    onAddPokemonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Catching,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Bu takımda henüz Pokemon yok",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Hadi takıma Pokemonlar ekleyelim!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddPokemonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(text = "Pokemon Ekle")
        }
    }
}

/**
 * Yeni Pokemon ekleme butonu
 */
@Composable
fun AddPokemonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(
            enabled = true,
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Yeni Pokemon Ekle",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Takım silme onay diyaloğu
 */
@Composable
fun DeleteTeamConfirmationDialog(
    teamName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Takımı Sil") },
        text = {
            Text("\"$teamName\" isimli takımı silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sil")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

// Yardımcı fonksiyonlar - Gerçek uygulamada daha anlamlı hesaplamalar yapılabilir
private fun calculateTeamPower(team: PokemonTeam): Int {
    if (team.pokemons.isEmpty()) return 0
    return team.pokemons.sumOf { it.attack } / team.pokemons.size
}

private fun calculateTeamDefense(team: PokemonTeam): Int {
    if (team.pokemons.isEmpty()) return 0
    return team.pokemons.sumOf { it.defense } / team.pokemons.size
}

private fun calculateTeamSpeed(team: PokemonTeam): Int {
    if (team.pokemons.isEmpty()) return 0
    return team.pokemons.sumOf { it.speed } / team.pokemons.size
} 