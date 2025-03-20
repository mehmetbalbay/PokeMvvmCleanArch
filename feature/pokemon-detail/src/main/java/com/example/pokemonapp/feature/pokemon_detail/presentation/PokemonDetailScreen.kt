package com.example.pokemonapp.feature.pokemon_detail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.pokemonapp.feature.pokemon_detail.domain.model.PokemonDetail
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    viewModel: PokemonDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    // Snackbar gösterimi için ekleyelim
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Pokemon yükleniyor...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.error ?: "Bilinmeyen hata",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadPokemonDetail() },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Text(
                                "Tekrar Dene",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                state.pokemonDetail != null -> {
                    val pokemonDetail = state.pokemonDetail!!
                    
                    PokemonDetailContent(
                        pokemonDetail = pokemonDetail,
                        onBackClick = onBackClick,
                        onFavoriteClick = { viewModel.toggleFavorite() },
                        isFavoriteActionInProgress = state.favoriteActionInProgress
                    )
                }
            }
        }
    }
}

@Composable
fun PokemonDetailContent(
    pokemonDetail: PokemonDetail,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavoriteActionInProgress: Boolean
) {
    // Varsayılan renk seçimi (dinamik olarak oluşturulan rastgele bir renk)
    val defaultColor = remember(pokemonDetail.id) {
        Color(
            red = (120..200).random(),
            green = (120..200).random(),
            blue = (120..200).random()
        )
    }
    
    // Dominant renk ve tip renk map'i
    val dominantColor = remember { mutableStateOf(defaultColor) }
    val typeColors = remember {
        mutableStateMapOf<String, Color>()
    }
    
    // Her tip için daha önce renk atanmış mı kontrol eder
    val typeColorAssigned = remember {
        mutableStateMapOf<String, Boolean>()
    }
    
    val context = LocalContext.current
    
    // Palette'ten çıkarılan renk varyasyonları
    val vibrantColor = remember { mutableStateOf<Color?>(null) }
    val lightVibrantColor = remember { mutableStateOf<Color?>(null) }
    val darkVibrantColor = remember { mutableStateOf<Color?>(null) }
    val mutedColor = remember { mutableStateOf<Color?>(null) }
    
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(pokemonDetail.imageUrl)
            .size(Size.ORIGINAL)
            .build()
    )
    
    // Renkleri palette API ile çıkar
    LaunchedEffect(painter.state) {
        try {
            if (painter.state is AsyncImagePainter.State.Success) {
                val drawable = (painter.state as AsyncImagePainter.State.Success).result.drawable
                if (drawable is BitmapDrawable && drawable.bitmap != null) {
                    Palette.from(drawable.bitmap).generate { palette ->
                        palette?.dominantSwatch?.rgb?.let { colorValue ->
                            val extractedColor = Color(colorValue)
                            dominantColor.value = extractedColor
                            
                            pokemonDetail.types.forEach { type ->
                                if (typeColorAssigned[type] != true) {
                                    typeColors[type] = extractedColor
                                    typeColorAssigned[type] = true
                                }
                            }
                        }
                        
                        palette?.vibrantSwatch?.rgb?.let {
                            vibrantColor.value = Color(it)
                        }
                        palette?.lightVibrantSwatch?.rgb?.let {
                            lightVibrantColor.value = Color(it)
                        }
                        palette?.darkVibrantSwatch?.rgb?.let {
                            darkVibrantColor.value = Color(it)
                        }
                        palette?.mutedSwatch?.rgb?.let {
                            mutedColor.value = Color(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Hata durumunda varsayılan rengi kullan
        }
    }
    
    // Farklı renk varyasyonlarını da kullanalım
    LaunchedEffect(painter.state) {
        try {
            if (painter.state is AsyncImagePainter.State.Success) {
                val drawable = (painter.state as AsyncImagePainter.State.Success).result.drawable
                if (drawable is BitmapDrawable && drawable.bitmap != null) {
                    Palette.from(drawable.bitmap).generate { palette ->
                        palette?.let { p ->
                            pokemonDetail.types.forEachIndexed { index, type ->
                                if (typeColorAssigned[type] != true) {
                                    val color = when(index % 3) {
                                        0 -> p.vibrantSwatch?.rgb
                                        1 -> p.lightVibrantSwatch?.rgb
                                        else -> p.darkVibrantSwatch?.rgb
                                    } ?: p.dominantSwatch?.rgb
                                    
                                    color?.let {
                                        typeColors[type] = Color(it)
                                        typeColorAssigned[type] = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Hata durumunda dominantColor'ı kullanmaya devam et
        }
    }
    
    // Tip için varsayılan renkler
    LaunchedEffect(key1 = Unit) {
        pokemonDetail.types.forEach { type ->
            if (typeColorAssigned[type] != true) {
                typeColors[type] = Color(
                    red = (30..240).random(),
                    green = (30..240).random(),
                    blue = (30..240).random()
                )
                typeColorAssigned[type] = true
            }
        }
    }
    
    // Metin için kontrast rengi hesapla (YIQ formülü)
    val r = dominantColor.value.red * 255
    val g = dominantColor.value.green * 255
    val b = dominantColor.value.blue * 255
    val yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000
    val textColor = if (yiq >= 128) {
        Color.Black.copy(alpha = 0.8f)
    } else {
        Color.White
    }
    
    // Arka plan gradyanı
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            dominantColor.value,
            dominantColor.value.copy(alpha = 0.7f),
            dominantColor.value.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.surface
        )
    )
    
    // Tüm ekranı kapsayan scroll
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Scroll edilebilir içerik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Üst banner (gradyan arka plan)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(backgroundGradient)
            ) {
                // Navigasyon butonları
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = dominantColor.value,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onFavoriteClick,
                        enabled = !isFavoriteActionInProgress,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .padding(4.dp)
                    ) {
                        if (isFavoriteActionInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = if (pokemonDetail.isFavorite) Color.Red else dominantColor.value
                            )
                        } else {
                            Icon(
                                imageVector = if (pokemonDetail.isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = if (pokemonDetail.isFavorite) {
                                    "Favorilerden Çıkar"
                                } else {
                                    "Favorilere Ekle"
                                },
                                tint = if (pokemonDetail.isFavorite) Color.Red else dominantColor.value,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // Pokemon resmi (büyük ve merkezi)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = pokemonDetail.imageUrl,
                            contentDescription = pokemonDetail.name,
                            modifier = Modifier.size(180.dp)
                        )
                    }
                }
            }
            
            // Pokemon adı ve ID'si
            Spacer(modifier = Modifier.height(80.dp))
            
    Column(
        modifier = Modifier
            .fillMaxWidth()
                    .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
                    text = pokemonDetail.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineLarge,
                    color = dominantColor.value,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "#${pokemonDetail.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tip etiketleri
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pokemonDetail.types.forEach { type ->
                        val typeColor = typeColors[type] ?: dominantColor.value
                        
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = typeColor
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = type.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Ana içerik - tüm detaylar kartlarda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 1. Fiziksel özellikler kartı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Başlık
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(dominantColor.value.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📊",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
            Text(
                                text = "Fiziksel Özellikler",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = dominantColor.value
                            )
                        }
                        
                        // Bilgiler
                        InfoRow(
                            title = "Boy", 
                            value = "${pokemonDetail.height / 10.0} m",
                            accentColor = vibrantColor.value ?: dominantColor.value
                        )
                        
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        InfoRow(
                            title = "Ağırlık", 
                            value = "${pokemonDetail.weight / 10.0} kg",
                            accentColor = vibrantColor.value ?: dominantColor.value
                        )
                        
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        InfoRow(
                            title = "Yetenekler", 
                            value = pokemonDetail.abilities.joinToString(", "),
                            accentColor = vibrantColor.value ?: dominantColor.value
                        )
                    }
                }
                
                // 2. İstatistikler kartı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Başlık
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(dominantColor.value.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
            Text(
                                    text = "📈",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "İstatistikler",
                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = dominantColor.value
                            )
                        }
                        
                        // Stat çubukları
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            pokemonDetail.stats.forEach { stat ->
                                // Stat başına farklı renk varyasyonları kullan
                                val statColor = when(stat.name) {
                                    "hp" -> darkVibrantColor.value
                                    "attack" -> vibrantColor.value
                                    "defense" -> lightVibrantColor.value
                                    "special-attack" -> mutedColor.value
                                    "special-defense" -> vibrantColor.value?.copy(alpha = 0.7f)
                                    "speed" -> darkVibrantColor.value?.copy(alpha = 0.7f)
                                    else -> dominantColor.value
                                } ?: dominantColor.value
                                
                                StatBar(
                                    statName = when (stat.name) {
                                        "hp" -> "HP"
                                        "attack" -> "Atak"
                                        "defense" -> "Savunma"
                                        "special-attack" -> "Özel Atak"
                                        "special-defense" -> "Özel Savunma"
                                        "speed" -> "Hız"
                                        else -> stat.name
                                    },
                                    statValue = stat.value,
                                    maxValue = 255,
                                    color = statColor
                                )
                            }
                        }
                    }
                }
                
                // 3. Hareketler kartı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Başlık
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(dominantColor.value.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
            Text(
                                text = "Hareketler",
                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = dominantColor.value
                            )
                        }
                        
                        // Hareketler listesi
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val displayMoves = pokemonDetail.moves.take(12)
                            val columns = 2
                            val rows = (displayMoves.size + columns - 1) / columns
                            
                            for (i in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    for (j in 0 until columns) {
                                        val index = i * columns + j
                                        if (index < displayMoves.size) {
                                            val move = displayMoves[index]
                                            MoveChip(
                                                moveName = move,
                                                color = dominantColor.value,
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                            
                            if (pokemonDetail.moves.size > 12) {
                                Text(
                                    text = "... ve ${pokemonDetail.moves.size - 12} adet daha",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MoveChip(
    moveName: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Text(
            text = moveName.replaceFirstChar { it.uppercase() },
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun InfoRow(
    title: String, 
    value: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = accentColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatBar(
    statName: String,
    statValue: Int,
    maxValue: Int,
    color: Color
) {
    val percentage = (statValue.toFloat() / maxValue).coerceIn(0f, 1f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
            
                Text(
                text = statValue.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.7f),
                                color
                            )
                        )
                    )
            )
        }
    }
} 