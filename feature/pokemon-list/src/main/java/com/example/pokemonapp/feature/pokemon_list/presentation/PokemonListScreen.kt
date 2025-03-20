package com.example.pokemonapp.feature.pokemon_list.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pokemonapp.core.ui.components.FullScreenError
import com.example.pokemonapp.core.ui.components.FullScreenLoading
import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow

/**
 * Pokemon listesi ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val availableTypes by viewModel.availableTypes.collectAsState()
    
    // Arama çubuğu aktif/pasif durumu
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Filtre dialog görünürlük durumu
    var showFilterDialog by remember { mutableStateOf(false) }
    
    // Snackbar için
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Favori mesajını göster
    LaunchedEffect(state.favoriteActionMessage) {
        state.favoriteActionMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            // Mesajı gösterdikten sonra temizle
            viewModel.clearFavoriteMessage()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Pokémon Listesi") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        // Filtre butonu
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Filtrele"
                            )
                        }
                        
                        // Favoriler butonu
                        IconButton(onClick = { viewModel.toggleShowFavoritesOnly() }) {
                            Icon(
                                imageVector = if (state.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (state.showFavoritesOnly) "Tüm Pokemonları Göster" else "Sadece Favorileri Göster",
                                tint = if (state.showFavoritesOnly) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                )
                
                // Daima görünür arama çubuğu
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { isSearchActive = false },
                    active = isSearchActive,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Pokémon ara...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ara"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Arama önerileri burada gösterilebilir
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableTypes) { type ->
                            Text(
                                text = "Tip: ${type.replaceFirstChar { it.uppercase() }}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!state.selectedTypes.contains(type)) {
                                            viewModel.toggleTypeFilter(type)
                                        }
                                        viewModel.updateSearchQuery("")
                                        isSearchActive = false
                                    }
                                    .padding(16.dp)
                            )
                        }
                        items(state.pokemons.take(5)) { pokemon ->
                            Text(
                                text = pokemon.name.replaceFirstChar { it.uppercase() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSearchQuery("")
                                        isSearchActive = false
                                        onPokemonClick(pokemon.id)
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Yükleme durumu
            if (state.isLoading && state.pokemons.isEmpty()) {
                FullScreenLoading(message = "Pokémonlar yükleniyor...")
            }
            // Hata durumu
            else if (state.error != null && state.pokemons.isEmpty()) {
                FullScreenError(
                    message = state.error ?: "Bilinmeyen bir hata oluştu",
                    onRetry = { viewModel.loadPokemons() }
                )
            }
            // Boş durum
            else if (state.isEmpty && state.pokemons.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pokémon bulunamadı",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            // İçerik
            else {
                Column {
                    // Aktif filtreler gösterimi
                    if (state.selectedTypes.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Filtreler:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            state.selectedTypes.take(3).forEach { type ->
                                FilterChip(
                                    selected = true,
                                    onClick = { viewModel.toggleTypeFilter(type) },
                                    label = { Text(type.replaceFirstChar { it.uppercase() }) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Filtreyi Kaldır"
                                        )
                                    }
                                )
                            }
                            
                            if (state.selectedTypes.size > 3) {
                                Text(
                                    text = "+${state.selectedTypes.size - 3}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Pokemon listesi
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredPokemons.ifEmpty { state.pokemons }) { pokemon ->
                            PokemonItem(
                                pokemon = pokemon,
                                onClick = { onPokemonClick(pokemon.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(pokemon.id) },
                                isFavoriteActionInProgress = state.favoriteActionInProgress.contains(pokemon.id)
                            )
                        }
                    }
                    
                    // Scroll sonuna geldiğinde daha fazla yükle
                    LaunchedEffect(listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
                        val layoutInfo = listState.layoutInfo
                        val totalItemsCount = layoutInfo.totalItemsCount
                        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        
                        if (lastVisibleItemIndex >= totalItemsCount - 5 && !state.isLoadingMore && state.hasMoreItems) {
                            viewModel.loadMorePokemons()
                        }
                    }
                }
                
                // Daha fazla yükleniyor göstergesi
                if (state.isLoadingMore) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
            
            // Filtre Dialog
            if (showFilterDialog) {
                TypeFilterDialog(
                    selectedTypes = state.selectedTypes,
                    availableTypes = availableTypes,
                    onDismiss = { showFilterDialog = false },
                    onConfirm = { selectedTypes ->
                        viewModel.setTypeFilters(selectedTypes)
                        showFilterDialog = false
                    }
                )
            }
        }
    }
}

/**
 * Pokémon tiplerini filtrelemek için dialog
 */
@Composable
fun TypeFilterDialog(
    selectedTypes: List<String>,
    availableTypes: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val tempSelectedTypes = remember { mutableStateListOf<String>().apply { addAll(selectedTypes) } }
    
    // Tip grupları
    val primaryTypes = listOf("normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground")
    val secondaryTypes = listOf("flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy")
    
    // Tüm tipleri seç/temizle
    val allSelected = remember(tempSelectedTypes) { tempSelectedTypes.size == availableTypes.size }
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pokémon Tiplerini Filtrele",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tümünü Seç / Temizle butonları
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            if (allSelected) {
                                tempSelectedTypes.clear()
                            } else {
                                tempSelectedTypes.clear()
                                tempSelectedTypes.addAll(availableTypes)
                            }
                        }
                    ) {
                        Text(
                            text = if (allSelected) "Tümünü Temizle" else "Tümünü Seç",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Seçilen filtre sayısı
                    Text(
                        text = "${tempSelectedTypes.size}/${availableTypes.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Filtre içeriği
                if (availableTypes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Birincil tipler
                        Text(
                            text = "Temel Tipler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(primaryTypes.filter { availableTypes.contains(it) }) { typeName ->
                                val isSelected = tempSelectedTypes.contains(typeName)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        if (isSelected) {
                                            tempSelectedTypes.remove(typeName)
                                        } else {
                                            tempSelectedTypes.add(typeName)
                                        }
                                    },
                                    label = { Text(typeName.replaceFirstChar { it.uppercase() }) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(getPokemonTypeColor(typeName))
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        selectedContainerColor = getPokemonTypeColor(typeName).copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // İkincil tipler
                        Text(
                            text = "Özel Tipler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(secondaryTypes.filter { availableTypes.contains(it) }) { typeName ->
                                val isSelected = tempSelectedTypes.contains(typeName)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        if (isSelected) {
                                            tempSelectedTypes.remove(typeName)
                                        } else {
                                            tempSelectedTypes.add(typeName)
                                        }
                                    },
                                    label = { Text(typeName.replaceFirstChar { it.uppercase() }) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(getPokemonTypeColor(typeName))
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        selectedContainerColor = getPokemonTypeColor(typeName).copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                        
                        // Diğer tipler (tanımlanan gruplarda olmayan tipler)
                        val otherTypes = availableTypes.filter { 
                            !primaryTypes.contains(it) && !secondaryTypes.contains(it) 
                        }
                        
                        if (otherTypes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Diğer Tipler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                items(otherTypes) { typeName ->
                                    val isSelected = tempSelectedTypes.contains(typeName)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { 
                                            if (isSelected) {
                                                tempSelectedTypes.remove(typeName)
                                            } else {
                                                tempSelectedTypes.add(typeName)
                                            }
                                        },
                                        label = { Text(typeName.replaceFirstChar { it.uppercase() }) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(getPokemonTypeColor(typeName))
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("İptal")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onConfirm(tempSelectedTypes.toList()) }
                    ) {
                        Text("Uygula")
                    }
                }
            }
        }
    }
}

/**
 * Pokemon liste öğesi kartı
 */
@Composable
fun PokemonItem(
    pokemon: Pokemon,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavoriteActionInProgress: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pokemon resmi
            Box(
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Pokemon ID ve adı
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${pokemon.id.toString().padStart(3, '0')}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = pokemon.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Pokemon tipleri
                Row {
                    pokemon.types.take(2).forEach { type ->
                        val backgroundColor = getPokemonTypeColor(type)
                        Surface(
                            modifier = Modifier.padding(end = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = backgroundColor
                        ) {
                            Text(
                                text = type.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Favori butonu
            IconButton(
                onClick = onFavoriteClick,
                enabled = !isFavoriteActionInProgress,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            ) {
                if (isFavoriteActionInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (pokemon.isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        imageVector = if (pokemon.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (pokemon.isFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                        tint = if (pokemon.isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Pokemon tipine göre renk döndürür
 */
@Composable
fun getPokemonTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "fire" -> Color(0xFFE57373)
        "water" -> Color(0xFF64B5F6)
        "grass" -> Color(0xFF8BC34A)
        "electric" -> Color(0xFFFFD54F)
        "psychic" -> Color(0xFFBA68C8)
        "ice" -> Color(0xFF80DEEA)
        "dragon" -> Color(0xFF7986CB)
        "dark" -> Color(0xFF616161)
        "fairy" -> Color(0xFFF8BBD0)
        "normal" -> Color(0xFFBDBDBD)
        "fighting" -> Color(0xFFBF360C)
        "flying" -> Color(0xFF90CAF9)
        "poison" -> Color(0xFFCE93D8)
        "ground" -> Color(0xFFBCAAA4)
        "rock" -> Color(0xFFFF7043)
        "bug" -> Color(0xFFAED581)
        "ghost" -> Color(0xFF9575CD)
        "steel" -> Color(0xFFB0BEC5)
        else -> Color(0xFFBDBDBD)
    }
} 