package com.example.pokemonapp.feature.pokemon_list.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel(),
    onPokemonClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val availableTypes by viewModel.availableTypes.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Lazy list state for pagination
    val lazyListState = rememberLazyListState()
    
    // Sayfalama için scroll listener
    LaunchedEffect(lazyListState) {
        snapshotFlow { 
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            
            // Son 10 öğeye geldiğimizde yeni veri yükleme işlemini başlat
            val shouldLoadMore = lastVisibleItemIndex > (totalItems - 10) && totalItems > 0
            
            shouldLoadMore
        }
        .distinctUntilChanged()
        .collect { shouldLoadMore ->
            // Herhangi bir filtreleme aktifse (arama, tip, favoriler), otomatik yükleme yapma
            val isFilterActive = state.searchQuery.isNotEmpty() || 
                                state.selectedType != null || 
                                state.showFavoritesOnly
            
            if (shouldLoadMore && !state.isLoading && !isFilterActive) {
                viewModel.loadPokemons()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokemon Listesi") },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavoritesOnly() }) {
                        Icon(
                            imageVector = if (state.showFavoritesOnly) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = if (state.showFavoritesOnly) {
                                "Tüm Pokemon'ları göster"
                            } else {
                                "Sadece favorileri göster"
                            }
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Sırala")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("ID'ye göre") },
                                onClick = {
                                    viewModel.updateSortOrder(SortOrder.ID)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("İsme göre (A-Z)") },
                                onClick = {
                                    viewModel.updateSortOrder(SortOrder.NAME_ASC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("İsme göre (Z-A)") },
                                onClick = {
                                    viewModel.updateSortOrder(SortOrder.NAME_DESC)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { },
                active = false,
                onActiveChange = { },
                placeholder = { Text("Pokemon ara...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedType == null,
                        onClick = { viewModel.updateSelectedType(null) },
                        label = { Text("Tümü") }
                    )
                }
                
                items(availableTypes) { type ->
                    FilterChip(
                        selected = state.selectedType == type,
                        onClick = { viewModel.updateSelectedType(type) },
                        label = { Text(type.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isLoading && state.pokemons.isEmpty() -> {
                        LoadingState()
                    }
                    state.error != null && state.pokemons.isEmpty() -> {
                        ErrorState(
                            message = state.error ?: "Pokemonları yüklerken bir hata oluştu. Lütfen tekrar deneyin.",
                            onRetry = { viewModel.refresh() }
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Filtreleme mantığı bir kez hesaplansın
                            val filteredPokemons = state.pokemons
                                .filter { pokemon ->
                                    val matchesSearch = pokemon.name.contains(state.searchQuery, ignoreCase = true)
                                    val matchesType = state.selectedType == null || pokemon.types.contains(state.selectedType)
                                    val matchesFavorites = !state.showFavoritesOnly || pokemon.isFavorite
                                    matchesSearch && matchesType && matchesFavorites
                                }
                                .let { pokemons ->
                                    when (state.sortOrder) {
                                        SortOrder.ID -> pokemons.sortedBy { it.id }
                                        SortOrder.NAME_ASC -> pokemons.sortedBy { it.name }
                                        SortOrder.NAME_DESC -> pokemons.sortedByDescending { it.name }
                                    }
                                }

                            // Filtrelemeyle ilgili bilgi göster
                            if (state.pokemons.isNotEmpty()) {
                                val isFilterActive = state.searchQuery.isNotEmpty() || 
                                                    state.selectedType != null || 
                                                    state.showFavoritesOnly
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = if (isFilterActive) {
                                            "Filtrelenmiş: ${filteredPokemons.size} Pokemon (Toplam: ${state.pokemons.size})"
                                        } else {
                                            "Toplam ${state.pokemons.size} Pokemon"
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }

                            if (state.pokemons.isEmpty() && !state.isLoading && state.error == null) {
                                // Veri yokken boş durum
                                EmptyState(
                                    message = "Henüz hiç Pokemon yok. Yüklemek için 'Yenile' düğmesine tıklayın.",
                                    actionText = "Yenile",
                                    onAction = { viewModel.refresh() },
                                    modifier = Modifier.weight(1f)
                                )
                            } else if (filteredPokemons.isEmpty() && state.pokemons.isNotEmpty()) {
                                // Filtreleme sonucu boş
                                val message = when {
                                    state.searchQuery.isNotEmpty() -> "\"${state.searchQuery}\" ile ilgili sonuç bulunamadı"
                                    state.selectedType != null -> "${state.selectedType} tipinde Pokemon bulunamadı"
                                    state.showFavoritesOnly -> "Henüz favori Pokemon eklenmemiş"
                                    else -> "Filtreleme sonucunda Pokemon bulunamadı"
                                }
                                
                                EmptyState(
                                    message = message,
                                    actionText = "Filtreleri Temizle",
                                    onAction = { 
                                        viewModel.updateSearchQuery("")
                                        viewModel.updateSelectedType(null)
                                        if (state.showFavoritesOnly) {
                                            viewModel.toggleFavoritesOnly()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                LazyColumn(
                                    state = lazyListState,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(filteredPokemons) { pokemon ->
                                        PokemonItem(
                                            pokemon = pokemon,
                                            onClick = { onPokemonClick(pokemon.id) },
                                            onFavoriteClick = { viewModel.toggleFavorite(pokemon.id) }
                                        )
                                    }
                                }
                                
                                // Alt kısımda yükleme durumlarını ve butonları göster
                                if (state.isLoading && state.searchQuery.isEmpty() && 
                                    state.selectedType == null && !state.showFavoritesOnly) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Daha fazla Pokemon yükleniyor...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                } else if (!state.isLoading && state.searchQuery.isEmpty() && 
                                           state.selectedType == null && !state.showFavoritesOnly) {
                                    // "Daha fazla yükle" butonu
                                    Button(
                                        onClick = { viewModel.loadMorePokemons() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text("Daha fazla yükle")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonItem(
    pokemon: Pokemon,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tipler: ${pokemon.types.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = if (pokemon.isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = if (pokemon.isFavorite) {
                        "Favorilerden çıkar"
                    } else {
                        "Favorilere ekle"
                    }
                )
            }
        }
    }
}

// Boş durumları göstermek için yardımcı composable fonksiyonlar ekleyelim
@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pokemonlar yükleniyor...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Tekrar Dene")
        }
    }
} 