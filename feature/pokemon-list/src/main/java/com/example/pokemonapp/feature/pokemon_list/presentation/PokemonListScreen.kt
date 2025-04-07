package com.example.pokemonapp.feature.pokemon_list.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pokemonapp.core.ui.components.PokemonCard
import com.example.pokemonapp.core.ui.model.PokemonItemUiModel
import com.example.pokemonapp.core.ui.theme.getPokemonTypeColor
import com.example.pokemonapp.feature.pokemon_list.presentation.PokemonListUiState.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.example.pokemonapp.core.ui.components.EmptyStateContent
import com.example.pokemonapp.core.ui.components.ErrorContent
import com.example.pokemonapp.core.ui.components.LoadingContent
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent

/**
 * Pokemon listesi ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    uiState: PokemonListUiState,
    onPokemonClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onToggleFavorite: (Int, Boolean) -> Unit,
    onTypeSelected: (String?) -> Unit,
    selectedType: String?,
    availableTypes: List<String>,
    viewModel: PokemonListViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val lastFavoriteAction = remember { mutableStateOf<Pair<Int, Boolean>?>(null) }
    
    // Favoriye ekleme/çıkarma durumunda Snackbar göster
    LaunchedEffect(lastFavoriteAction.value) {
        lastFavoriteAction.value?.let { (pokemonId, isFavorite) ->
            val message = if (isFavorite) {
                "Pokémon #$pokemonId favorilere eklendi"
            } else {
                "Pokémon #$pokemonId favorilerden çıkarıldı"
            }
            snackbarHostState.showSnackbar(message)
            lastFavoriteAction.value = null
        }
    }

    val favoriteActionWrapper: (Int, Boolean) -> Unit = { pokemonId, isFavorite ->
        onToggleFavorite(pokemonId, isFavorite)
        lastFavoriteAction.value = pokemonId to isFavorite
    }

    val gridState = rememberLazyGridState()
    
    LaunchedEffect(uiState) {
        if (uiState is PokemonListUiState.Success && uiState.scrollToTop) {
            gridState.scrollToItem(0)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pokédex",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Arama Çubuğu
                SearchBar(
                    searchQuery = when(uiState) {
                        is PokemonListUiState.Success -> uiState.searchQuery
                        else -> ""
                    },
                    onSearchQueryChange = onSearchQueryChange,
                    onSearch = onSearch
                )

                // Tip Filtreleri
                if (uiState is PokemonListUiState.Success) {
                    PokemonTypeFilters(
                        selectedType = selectedType,
                        onTypeSelected = onTypeSelected,
                        availableTypes = availableTypes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Ana içerik
                when (uiState) {
                    is Error -> {
                        ErrorState(onRetry = onRetry)
                    }
                    is Loading -> {
                        LoadingState()
                    }
                    is Success -> {
                        if (uiState.pokemons.isEmpty()) {
                            EmptyState()
                        } else {
                            PokemonGrid(
                                pokemons = uiState.pokemons,
                                onPokemonClick = onPokemonClick,
                                onToggleFavorite = { pokemonId, currentFavoriteState ->
                                    favoriteActionWrapper(pokemonId, !currentFavoriteState)
                                },
                                gridState = gridState,
                                isLoadingMore = uiState.isLoadingMore,
                                hasMoreItems = uiState.hasMoreItems,
                                onLoadMore = viewModel::loadMorePokemons
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Pokemon ara...") },
        leadingIcon = { 
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ara"
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        )
    )
}

@Composable
fun PokemonTypeFilters(
    selectedType: String?,
    onTypeSelected: (String?) -> Unit,
    availableTypes: List<String>,
    modifier: Modifier = Modifier
) {
    // Tip seçimini izle ve debug için kaydet
    LaunchedEffect(selectedType) {
        println("### PokemonTypeFilters: Seçilen tip değişti: $selectedType")
    }
    
    // Mevcut tipleri izle
    LaunchedEffect(availableTypes) {
        println("### PokemonTypeFilters: Tip listesi güncellendi, ${availableTypes.size} tip var: $availableTypes")
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Başlık ve temizleme butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtre ikonu",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tipe Göre Filtrele",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Seçim bilgisi ve temizleme butonu
                AnimatedVisibility(
                    visible = selectedType != null,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = getTypeColor(selectedType ?: "").copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = selectedType?.replaceFirstChar { it.uppercase() } ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = getTypeColor(selectedType ?: ""),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = { 
                                println("### PokemonTypeFilters: Filtre temizleme butonu tıklandı")
                                onTypeSelected(null) 
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Filtreyi temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            if (availableTypes.isNotEmpty()) {
                // Tip sayısı bilgisi
                Text(
                    text = "${availableTypes.size} farklı tip bulundu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Yatay kaydırılabilir tip listesi
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    // "Tümü" seçeneği
                    item {
                        FilterChipItem(
                            label = "Tümü",
                            selected = selectedType == null,
                            onClick = { 
                                println("### PokemonTypeFilters: 'Tümü' filtresi seçildi")
                                onTypeSelected(null) 
                            }
                        )
                    }
                    
                    // Tip listesi
                    items(availableTypes) { type ->
                        FilterChipItem(
                            label = type.replaceFirstChar { it.uppercase() },
                            selected = type == selectedType,
                            onClick = { 
                                println("### PokemonTypeFilters: '$type' filtresi seçildi")
                                onTypeSelected(type) 
                            },
                            selectedColor = getTypeColor(type)
                        )
                    }
                }
            } else {
                // Tip listesi boşsa yükleme göstergesi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tip listesi yükleniyor...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        selectedColor.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val borderColor = if (selected) {
        selectedColor
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    
    val textColor = if (selected) {
        selectedColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = if (selected) 2.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (selected) selectedColor else Color.Transparent
            ),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seçildi",
                    tint = textColor,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * Pokémon tipine göre renk döndüren yardımcı fonksiyon
 */
@Composable
fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "normal" -> Color(0xFFA8A77A)
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

/**
 * Tipe göre karşıt renk döndüren yardımcı fonksiyon
 */
@Composable
fun getContrastColor(color: Color): Color {
    // Rengin parlaklığını hesapla
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    // Parlak renkler için koyu metin, koyu renkler için açık metin kullan
    return if (luminance > 0.5f) Color.Black.copy(alpha = 0.8f) else Color.White
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonGrid(
    pokemons: List<PokemonItemUiModel>,
    onPokemonClick: (Int) -> Unit,
    onToggleFavorite: (Int, Boolean) -> Unit,
    gridState: LazyGridState,
    isLoadingMore: Boolean = false,
    hasMoreItems: Boolean = true,
    onLoadMore: () -> Unit = {}
) {
    // Sayfalama için kaydırma durumunu izle
    val loadMoreThreshold = 4 // Son 4 öğe göründüğünde yeni içerik yükle
    
    // Loading durumunu izleyen state
    var isLoadingVisible by remember { mutableStateOf(false) }
    var loadingVisibleTime by remember { mutableStateOf(0L) }
    
    // Loading görünürlüğünü kontrol et
    LaunchedEffect(isLoadingMore, hasMoreItems) {
        println("### Debug - PokemonGrid: isLoadingMore=$isLoadingMore, hasMoreItems=$hasMoreItems")
        
        if (isLoadingMore && hasMoreItems) {
            // Sadece daha fazla öğe varsa ve yükleme aktifse göster
            isLoadingVisible = true
            loadingVisibleTime = System.currentTimeMillis()
        } else {
            // Yükleme bittiğinde veya daha fazla öğe yoksa gizle
            isLoadingVisible = false
        }
    }
    
    // Yükleme çok uzun sürerse otomatik olarak gizle (güvenlik kontrolü)
    LaunchedEffect(isLoadingVisible) {
        if (isLoadingVisible) {
            // 8 saniye sonra kontrol et
            delay(8000)
            // Hala yükleme gösteriliyor ve çok uzun zaman geçtiyse gizle
            val currentTime = System.currentTimeMillis()
            if (isLoadingVisible && (currentTime - loadingVisibleTime > 8000)) {
                println("### Debug - PokemonGrid: Yükleme göstergesi timeout, gizleniyor")
                isLoadingVisible = false
            }
        }
    }
    
    // Sayfalama için LaunchedEffect
    LaunchedEffect(pokemons.size, isLoadingMore, hasMoreItems, gridState.firstVisibleItemIndex) {
        // Scroll pozisyonu değiştiğinde veya veri değişiminde kontrol et
        if (pokemons.isEmpty() || isLoadingMore || !hasMoreItems) {
            return@LaunchedEffect
        }
        
        // Görünür son öğelerin indekslerini al
        val layoutInfo = gridState.layoutInfo
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        
        if (visibleItemsInfo.isEmpty()) {
            return@LaunchedEffect
        }
        
        // Son görünür öğe listede nerede?
        val lastVisibleItemIndex = visibleItemsInfo.last().index
        
        // Liste sonuna yaklaşılıyor mu?
        val isCloseToBottom = lastVisibleItemIndex >= pokemons.size - loadMoreThreshold
        
        if (isCloseToBottom) {
            println("### Pagination: Liste sonuna yaklaşıldı - (son görünür: $lastVisibleItemIndex/${pokemons.size}) - daha fazla yükleniyor")
            onLoadMore()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 8.dp, 
                end = 8.dp, 
                top = 8.dp, 
                // Yükleme göstergesinin yüksekliği kadar ekstra bottom padding ekle
                bottom = if (isLoadingVisible) 100.dp else 8.dp
            ),
            state = gridState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(pokemons) { pokemon ->
                PokemonCard(
                    pokemon = pokemon,
                    onClick = { onPokemonClick(pokemon.id) },
                    onFavoriteClick = { isFavorite -> onToggleFavorite(pokemon.id, isFavorite) },
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            // Liste sonu göstergesi
            if (!hasMoreItems && pokemons.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tüm Pokemonları gördünüz!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Sayfa altında sabit yükleme göstergesi - mutlaka sayfa içeriğinin ÜSTÜNDE gösterilmesi için zIndex kullanıyoruz
        AnimatedVisibility(
            visible = isLoadingVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .zIndex(10f) // Önemli: zIndex sayesinde diğer elemanların üstünde görünecek
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "Daha fazla Pokemon yükleniyor...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pokemon bulunamadı",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Arama kriterlerini değiştirmeyi veya filtreleri temizlemeyi deneyin.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
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
                text = "Pokemon listesi yüklenirken bir sorun oluştu.",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pokemonlar yükleniyor...",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar(
        searchQuery = "Pikachu",
        onSearchQueryChange = {},
        onSearch = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PokemonTypeFiltersPreview() {
    PokemonTypeFilters(
        selectedType = "fire",
        onTypeSelected = {},
        availableTypes = listOf("normal", "fire", "water", "grass", "electric", "ice", "fighting", "poison", "ground")
    )
}

@Preview(showBackground = true)
@Composable
fun ErrorStatePreview() {
    ErrorState(onRetry = {}, modifier = Modifier)
}

@Preview(showBackground = true)
@Composable
fun LoadingStatePreview() {
    LoadingState(modifier = Modifier)
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    EmptyState(modifier = Modifier)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PokemonListScreenPreview() {
    val samplePokemons = listOf(
        PokemonItemUiModel(
            id = 1,
            name = "bulbasaur",
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            types = listOf("grass", "poison")
        ),
        PokemonItemUiModel(
            id = 4,
            name = "charmander",
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png",
            types = listOf("fire")
        ),
        PokemonItemUiModel(
            id = 7,
            name = "squirtle",
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png",
            types = listOf("water")
        )
    )
    
    PokemonListScreen(
        uiState = Success(
            pokemons = samplePokemons,
            searchQuery = "",
            selectedType = null,
            scrollToTop = false
        ),
        onPokemonClick = {},
        onRetry = {},
        onSearchQueryChange = {},
        onSearch = {},
        onToggleFavorite = { _, _ -> },
        onTypeSelected = {},
        selectedType = null,
        availableTypes = listOf("grass", "poison", "fire", "water"),
        viewModel = hiltViewModel()
    )
} 