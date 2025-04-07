package com.example.pokemonapp.feature.pokemon_list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonapp.core.common.Resource
import com.example.pokemonapp.core.ui.model.PokemonItemUiModel
import com.example.pokemonapp.feature.pokemon_list.domain.model.Pokemon
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.GetPokemonDetailUseCase
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.GetPokemonListUseCase
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.GetPokemonTypesUseCase
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.ObserveFavoritePokemonsUseCase
import com.example.pokemonapp.feature.pokemon_list.domain.usecase.ToggleFavoritePokemonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pokemon Listesi için ViewModel.
 * MVI mimarisini kullanarak durumu yönetir.
 */
@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val getPokemonListUseCase: GetPokemonListUseCase,
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val getPokemonTypesUseCase: GetPokemonTypesUseCase,
    private val toggleFavoritePokemonUseCase: ToggleFavoritePokemonUseCase,
    private val observeFavoritePokemonsUseCase: ObserveFavoritePokemonsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()
    
    private val _availableTypes = MutableStateFlow<List<String>>(emptyList())
    val availableTypes: StateFlow<List<String>> = _availableTypes.asStateFlow()

    // İç durum takibi için daha detaylı state
    private val _state = MutableStateFlow(PokemonListState())

    init {
        loadPokemons()
        observeFavorites()
        // Tip listesini doğrudan API'den yükle
        updateAvailableTypes()
    }

    /**
     * Pokemon listesini yükler
     */
    fun loadPokemons() {
        viewModelScope.launch {
            _uiState.value = PokemonListUiState.Loading
            
            getPokemonListUseCase(offset = 0, limit = _state.value.limit)
                .catch { e ->
                    _uiState.value = PokemonListUiState.Error(
                        message = "Pokemon yüklenirken hata oluştu: ${e.message}"
                    )
                }
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = PokemonListUiState.Loading
                        }
                        is Resource.Success -> {
                            val pokemons = result.data?.pokemons ?: emptyList()
                            _state.update { it.copy(
                                pokemons = pokemons,
                                currentOffset = _state.value.limit,
                                totalCount = result.data?.count ?: 0,
                                hasMoreItems = (_state.value.limit < (result.data?.count ?: 0)),
                                isEmpty = pokemons.isEmpty(),
                                isLoading = false,
                                isLoadingMore = false
                            )}
                            
                            // Tip listesini güncelle
                            updateAvailableTypes()
                            
                            // UI state'i güncelle
                            updateUiState()
                        }
                        is Resource.Error -> {
                            _uiState.value = PokemonListUiState.Error(
                                message = result.message ?: "Bilinmeyen bir hata oluştu"
                            )
                        }
                        is Resource.Empty -> {
                            _uiState.value = PokemonListUiState.Success(
                                pokemons = emptyList()
                            )
                        }
                    }
                }
        }
    }

    /**
     * Daha fazla Pokemon yükler (sayfalama)
     */
    fun loadMorePokemons() {
        val currentState = _state.value
        
        // Eğer zaten yükleme yapılıyorsa veya daha fazla öğe yoksa return
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMoreItems) {
            println("### Pagination: Yükleme durumu zaten aktif veya daha fazla öğe yok - isLoading=${currentState.isLoading}, isLoadingMore=${currentState.isLoadingMore}, hasMoreItems=${currentState.hasMoreItems}")
            
            // İstek zaten yapıldıysa ve uzun süredir yükleme durumunda kaldıysa, reset
            if (currentState.isLoadingMore) {
                // isLoadingMore durumunu sıfırla, loopa girmesini önle
                _state.update { it.copy(isLoadingMore = false) }
                updatePaginationUiState()
            }
            return
        }
        
        // Aktif bir arama veya filtre var mı kontrol et
        val hasActiveFilters = currentState.searchQuery.isNotEmpty() || currentState.selectedTypes.isNotEmpty()
        
        if (hasActiveFilters) {
            // Tip filtresi aktifken lokal filtreleme yapıyoruz, daha fazla veri yükleyerek filtreleme yapabiliriz
            println("### Pagination: Filtre aktif, filtrelemeye devam etmek için daha fazla Pokemon yükleniyor")
            // isLoadingMore'u true yap ve ui'ı güncelle
            _state.update { it.copy(isLoadingMore = true) }
            updatePaginationUiState()
            
            // Tüm verileri yüklenmediği için daha fazla veri getir
            if (currentState.currentOffset < currentState.totalCount) {
                loadMoreFilteredPokemons(currentState)
            } else {
                // Tüm veriler zaten yüklendi, hasMoreItems false yap
                println("### Pagination: Filtreleme için tüm veriler zaten yüklenmiş")
                _state.update { it.copy(isLoadingMore = false, hasMoreItems = false) }
                updatePaginationUiState()
            }
            return
        }
        
        // Mevcut offset, toplam sayıya eşit veya daha büyükse daha fazla yüklenecek öğe yok demektir
        if (currentState.currentOffset >= currentState.totalCount) {
            println("### Pagination: Liste sonuna ulaşıldı, offset=${currentState.currentOffset}, totalCount=${currentState.totalCount}")
            _state.update { it.copy(hasMoreItems = false, isLoadingMore = false) }
            updatePaginationUiState()
            return
        }
        
        viewModelScope.launch {
            // Önce yükleniyor durumunu güncelle
            _state.update { it.copy(isLoadingMore = true) }
            
            // UI state'i hemen güncelle ki loading göstergesi görünsün
            println("### Pagination: isLoadingMore=true olarak ayarlandı")
            updatePaginationUiState()
            
            // Timeout mekanizması - yükleme aşırı uzun sürerse iptal et
            val timeoutJob = viewModelScope.launch {
                kotlinx.coroutines.delay(10000) // 10 saniye timeout
                if (_state.value.isLoadingMore) {
                    println("### Pagination: Yükleme zaman aşımına uğradı!")
                    _state.update { it.copy(isLoadingMore = false) }
                    updatePaginationUiState()
                }
            }
            
            println("### Pagination: Daha fazla Pokemon yükleniyor - offset=${currentState.currentOffset}, limit=${currentState.limit}")
            
            try {
                getPokemonListUseCase(
                    offset = currentState.currentOffset,
                    limit = currentState.limit
                ).catch { e ->
                    println("### Pagination: Pokemon yükleme hatası: ${e.message}")
                    _state.update { it.copy(isLoadingMore = false) }
                    updatePaginationUiState()
                    timeoutJob.cancel() // Hata durumunda timeout iptal
                }.collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            timeoutJob.cancel() // Başarılı durumda timeout iptal
                            
                            val newPokemons = result.data?.pokemons ?: emptyList()
                            val totalItemCount = result.data?.count ?: 0
                            
                            // Yeni Pokemon'ları mevcut listeye ekle
                            val updatedPokemons = currentState.pokemons + newPokemons
                            val newOffset = currentState.currentOffset + newPokemons.size
                            
                            // Eğer yeni offset toplam sayıya eşit veya daha büyükse, hasMoreItems false olmalı
                            val hasMore = newOffset < totalItemCount && newPokemons.isNotEmpty()
                            
                            println("### Pagination: Yeni Pokemon'lar yüklendi - yeni: ${newPokemons.size}, toplam: ${updatedPokemons.size}")
                            println("### Pagination: Yeni offset: $newOffset / toplam: $totalItemCount (Daha fazla var: $hasMore)")
                            
                            _state.update { it.copy(
                                pokemons = updatedPokemons,
                                currentOffset = newOffset,
                                totalCount = totalItemCount,
                                hasMoreItems = hasMore,
                                isLoadingMore = false
                            )}
                            
                            // Tip listesini güncelle
                            updateAvailableTypes()
                            
                            // UI state'i tam olarak güncelle (isLoadingMore = false)
                            updateUiState()
                        }
                        is Resource.Error -> {
                            timeoutJob.cancel() // Hata durumunda timeout iptal
                            println("### Pagination: Hata - ${result.message}")
                            _state.update { it.copy(
                                isLoadingMore = false,
                                error = result.message,
                                hasMoreItems = false  // Hata durumunda daha fazla yüklemeyi durdur
                            )}
                            updatePaginationUiState()
                        }
                        is Resource.Empty -> {
                            timeoutJob.cancel() // Boş durumda timeout iptal
                            println("### Pagination: Sonuç boş veya liste sonu")
                            _state.update { it.copy(
                                hasMoreItems = false,
                                isLoadingMore = false
                            )}
                            updatePaginationUiState()
                        }
                        // Loading durumunu göster
                        is Resource.Loading -> {
                            println("### Pagination: Yükleniyor")
                            // Burada isLoadingMore'u tekrar true yapmıyoruz, zaten aktif
                        }
                        else -> {
                            timeoutJob.cancel() // Diğer durumlarda timeout iptal
                            _state.update { it.copy(isLoadingMore = false, hasMoreItems = false) }
                            updatePaginationUiState()
                        }
                    }
                }
            } catch (e: Exception) {
                println("### Pagination: Genel hata - ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(isLoadingMore = false, hasMoreItems = false) }
                updatePaginationUiState()
                timeoutJob.cancel() // Hata durumunda timeout iptal
            }
        }
    }

    /**
     * Filtreler aktifken daha fazla Pokemon yükleme işlemi
     */
    private fun loadMoreFilteredPokemons(currentState: PokemonListState) {
        viewModelScope.launch {
            // UI state'i hemen güncelle ki loading göstergesi görünsün
            println("### Pagination: Filtre için daha fazla Pokemon yükleniyor")
            
            // Timeout mekanizması
            val timeoutJob = viewModelScope.launch {
                kotlinx.coroutines.delay(10000) // 10 saniye timeout
                if (_state.value.isLoadingMore) {
                    println("### Pagination: Filtreleme yüklemesi zaman aşımına uğradı!")
                    _state.update { it.copy(isLoadingMore = false) }
                    updatePaginationUiState()
                }
            }
            
            try {
                getPokemonListUseCase(
                    offset = currentState.currentOffset,
                    limit = currentState.limit
                ).catch { e ->
                    println("### Pagination: Filtreleme için Pokemon yükleme hatası: ${e.message}")
                    _state.update { it.copy(isLoadingMore = false) }
                    updatePaginationUiState()
                    timeoutJob.cancel()
                }.collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            timeoutJob.cancel()
                            
                            val newPokemons = result.data?.pokemons ?: emptyList()
                            val totalItemCount = result.data?.count ?: 0
                            
                            // Yeni Pokemon'ları mevcut listeye ekle
                            val updatedPokemons = currentState.pokemons + newPokemons
                            val newOffset = currentState.currentOffset + newPokemons.size
                            
                            // Eğer yeni offset toplam sayıya eşit veya daha büyükse, hasMoreItems false olmalı
                            val hasMore = newOffset < totalItemCount && newPokemons.isNotEmpty()
                            
                            println("### Pagination: Filtreleme için yeni Pokemon'lar yüklendi - yeni: ${newPokemons.size}, toplam: ${updatedPokemons.size}")
                            
                            _state.update { it.copy(
                                pokemons = updatedPokemons,
                                currentOffset = newOffset,
                                totalCount = totalItemCount,
                                hasMoreItems = hasMore,
                                isLoadingMore = false
                            )}
                            
                            // Yeni Pokemon'lar eklendiğinden tip listesini güncelle
                            updateAvailableTypes()
                            
                            // UI state'i güncelle - filtreleri uygulayarak
                            updateUiState()
                        }
                        is Resource.Error -> {
                            timeoutJob.cancel()
                            println("### Pagination: Filtreleme hatası - ${result.message}")
                            _state.update { it.copy(isLoadingMore = false, hasMoreItems = false) }
                            updatePaginationUiState()
                        }
                        is Resource.Empty -> {
                            timeoutJob.cancel()
                            println("### Pagination: Filtreleme sonucu boş")
                            _state.update { it.copy(isLoadingMore = false, hasMoreItems = false) }
                            updatePaginationUiState()
                        }
                        else -> {
                            // Loading durumunda bir şey yapma
                        }
                    }
                }
            } catch (e: Exception) {
                println("### Pagination: Filtreleme sırasında genel hata - ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(isLoadingMore = false, hasMoreItems = false) }
                updatePaginationUiState()
                timeoutJob.cancel()
            }
        }
    }

    /**
     * Pagination UI durumunu güncelle
     */
    private fun updatePaginationUiState() {
        val currentUiState = _uiState.value
        if (currentUiState is PokemonListUiState.Success) {
            // İç state'i kontrol et
            val stateLoadingMore = _state.value.isLoadingMore
            val uiLoadingMore = currentUiState.isLoadingMore
            val stateHasMoreItems = _state.value.hasMoreItems
            
            // Debug bilgisi
            if (stateLoadingMore != uiLoadingMore || stateHasMoreItems != currentUiState.hasMoreItems) {
                println("### Pagination: UI güncelleniyor - State: isLoadingMore=$stateLoadingMore, hasMoreItems=$stateHasMoreItems | UI: isLoadingMore=$uiLoadingMore, hasMoreItems=${currentUiState.hasMoreItems}")
            }
            
            // UI'ı güncelleyerek state ile uyumlu olmasını sağla
            val updatedState = currentUiState.copy(
                isLoadingMore = stateLoadingMore,
                hasMoreItems = stateHasMoreItems
            )
            
            _uiState.value = updatedState
        }
    }

    /**
     * Arama sorgusunu günceller
     */
    fun setSearchQuery(query: String) {
        _state.update { 
            it.copy(
                searchQuery = query,
                // Arama sorgusu değiştiğinde pagination sıfırlanır
                hasMoreItems = query.isEmpty() && it.selectedTypes.isEmpty()
            ) 
        }
        updateUiState()
    }

    /**
     * Arama işlemini tetikler
     */
    fun searchPokemons() {
        applyFilters()
    }

    /**
     * Arama işlemini uygula ve UI'ı güncelle
     */
    private fun applyFilters() {
        updateUiState()
    }

    /**
     * Yukarı kaydırma işlemini tetikler
     */
    fun scrollToTop() {
        val currentUiState = _uiState.value
        if (currentUiState is PokemonListUiState.Success) {
            _uiState.value = currentUiState.copy(scrollToTop = true)
            // Scroll tamamlandıktan sonra flag'i reset et
            viewModelScope.launch {
                _uiState.value = currentUiState.copy(scrollToTop = false)
            }
        }
    }

    /**
     * Seçilen Pokemon tipini günceller
     */
    fun setSelectedType(type: String?) {
        println("### Tip Filtresi: Seçilen tip güncelleniyor: $type")
        
        if (_state.value.selectedTypes.firstOrNull() == type) {
            println("### Tip Filtresi: Seçilen tip zaten aktif, değişiklik yok")
            return
        }
        
        // Mevcut durumu yakala
        val currentState = _state.value
        val isFilterActive = type != null
        
        // Önce yükleme göstergesini aktif et ve UI'ı güncelle
        _state.update { 
            it.copy(
                isLoading = true, 
                selectedTypes = if (type != null) listOf(type) else emptyList()
            ) 
        }
        
        // UI state'i hemen güncelle
        _uiState.value = PokemonListUiState.Loading
        
        viewModelScope.launch {
            // Eğer yeterli veri yoksa ve filtre aktifse, daha fazla veri getir
            if (isFilterActive && currentState.pokemons.size < 100) { // En azından ilk 100 veriyi göster
                try {
                    // Daha fazla veri yükle
                    val targetLimit = 100 // Hedef yükleme limiti
                    val remainingToLoad = targetLimit - currentState.pokemons.size
                    
                    if (remainingToLoad > 0) {
                        println("### Tip Filtresi: $type filtresi için daha fazla veri yükleniyor. Mevcut: ${currentState.pokemons.size}, Hedef: $targetLimit")
                        
                        // Yeni veri yükle
                        getPokemonListUseCase(
                            offset = currentState.currentOffset,
                            limit = remainingToLoad.coerceAtMost(20) // Maksimum 20 veri al
                        ).catch { e ->
                            println("### Tip Filtresi: Veri yükleme hatası - ${e.message}")
                        }.collectLatest { result ->
                            when (result) {
                                is Resource.Success -> {
                                    val newPokemons = result.data?.pokemons ?: emptyList()
                                    val totalCount = result.data?.count ?: 0
                                    
                                    // Yeni Pokemon'ları mevcut listeye ekle
                                    val updatedPokemons = currentState.pokemons + newPokemons
                                    val newOffset = currentState.currentOffset + newPokemons.size
                                    
                                    println("### Tip Filtresi: ${newPokemons.size} yeni Pokemon yüklendi, toplam: ${updatedPokemons.size}")
                                    
                                    // State'i güncelle
                                    _state.update { it.copy(
                                        pokemons = updatedPokemons,
                                        currentOffset = newOffset,
                                        totalCount = totalCount,
                                        // Filtre aktifse sayfalama özelliğini koru
                                        hasMoreItems = isFilterActive,
                                        isLoading = false
                                    )}
                                    
                                    // Tip listesini güncelle
                                    updateAvailableTypes()
                                    
                                    // Filtreleri uygula ve UI state'i güncelle
                                    updateUiState()
                                }
                                else -> {
                                    // Diğer durumlarda varsayılan güncellemeyi yap
                                    _state.update { it.copy(
                                        selectedTypes = if (type != null) listOf(type) else emptyList(),
                                        hasMoreItems = isFilterActive,
                                        isLoading = false
                                    )}
                                    updateUiState()
                                }
                            }
                        }
                    } else {
                        // Zaten yeterli veri var, sadece filtreleri uygula
                        _state.update { it.copy(
                            selectedTypes = if (type != null) listOf(type) else emptyList(),
                            hasMoreItems = isFilterActive,
                            isLoading = false
                        )}
                        updateUiState()
                    }
                } catch (e: Exception) {
                    println("### Tip Filtresi: Hata - ${e.message}")
                    e.printStackTrace()
                    
                    // Hata durumunda da filtreleri uygula
                    _state.update { it.copy(
                        selectedTypes = if (type != null) listOf(type) else emptyList(),
                        hasMoreItems = isFilterActive,
                        isLoading = false
                    )}
                    updateUiState()
                }
            } else {
                // Yeterli veri var veya filtre temizlendi, direkt güncelle
                _state.update { it.copy(
                    selectedTypes = if (type != null) listOf(type) else emptyList(),
                    // Filtre aktifse sayfalama özelliğini koru, değilse sayfalamayı etkinleştir
                    hasMoreItems = type == null || currentState.currentOffset < currentState.totalCount,
                    isLoading = false
                )}
                println("### Tip Filtresi: State güncellendi, selectedTypes=${_state.value.selectedTypes}, hasMoreItems=${_state.value.hasMoreItems}")
                updateUiState()
            }
        }
    }

    /**
     * İç durum değişikliklerini UI durumuna yansıt
     */
    private fun updateUiState() {
        val currentState = _state.value
        
        // Loading durumu kontrolü
        if (currentState.isLoading) {
            _uiState.value = PokemonListUiState.Loading
            return
        }
        
        // Domain modellerini UI modellerine dönüştür
        val pokemonItems = currentState.pokemons.map { it.toUiModel() }
        
        // Aktif filter var mı?
        val hasActiveFilters = currentState.searchQuery.isNotEmpty() || currentState.selectedTypes.isNotEmpty()
        
        // Filtreleri uygula
        val filteredPokemonItems = if (hasActiveFilters) {
            applyFilters(pokemonItems)
        } else {
            pokemonItems
        }
        
        // Filtreleme sonucu boş ise
        if (filteredPokemonItems.isEmpty() && hasActiveFilters) {
            println("### UI State: Filtre sonucu boş, daha fazla veri yüklenebilir: hasMoreItems=${currentState.hasMoreItems}")
        }
        
        // Sayfalama durumu - Filtre aktifken ve hiç sonuç yoksa, hasMoreItems true yaparak daha fazla yüklemeyi dene
        val shouldAllowMoreItems = when {
            // Filtre aktif ve sonuç boş ise, daha fazla yüklemek için true
            filteredPokemonItems.isEmpty() && hasActiveFilters -> true
            // Filtre aktif ama sonuç var, devam et
            hasActiveFilters -> currentState.hasMoreItems
            // Normal durum
            else -> currentState.hasMoreItems
        }
        
        // UI durumunu güncelle
        _uiState.value = PokemonListUiState.Success(
            pokemons = filteredPokemonItems,
            searchQuery = currentState.searchQuery,
            selectedType = currentState.selectedTypes.firstOrNull(),
            isLoadingMore = currentState.isLoadingMore,
            hasMoreItems = shouldAllowMoreItems
        )
        
        println("### UI State: Toplam ${filteredPokemonItems.size} Pokemon gösteriliyor. " +
                 "Filtre: ${if (hasActiveFilters) "Aktif" else "Pasif"}, " +
                 "isLoadingMore: ${currentState.isLoadingMore}, " +
                 "hasMoreItems: $shouldAllowMoreItems")
    }

    /**
     * UI modellerini filtrele
     */
    private fun applyFilters(pokemonItems: List<PokemonItemUiModel>): List<PokemonItemUiModel> {
        val currentState = _state.value
        var filteredItems = pokemonItems
        
        // Arama sorgusuna göre filtrele
        if (currentState.searchQuery.isNotEmpty()) {
            val lowerCaseQuery = currentState.searchQuery.lowercase()
            filteredItems = filteredItems.filter { pokemon -> 
                // İsim içinde arama
                val nameMatch = pokemon.name.lowercase().contains(lowerCaseQuery)
                // ID içinde arama
                val idMatch = pokemon.id.toString().contains(lowerCaseQuery)
                // Tip içinde arama
                val typeMatch = pokemon.types.any { it.lowercase().contains(lowerCaseQuery) }
                
                nameMatch || idMatch || typeMatch
            }
        }
        
        // Seçili türlere göre filtrele
        if (currentState.selectedTypes.isNotEmpty()) {
            filteredItems = filteredItems.filter { pokemon ->
                currentState.selectedTypes.any { selectedType ->
                    pokemon.types.any { pokemonType ->
                        pokemonType.equals(selectedType, ignoreCase = true)
                    }
                }
            }
        }
        
        return filteredItems
    }

    /**
     * Pokemon favorileme durumunu değiştirir
     */
    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            try {
                val oldIsFavorite = _state.value.favoriteIds.contains(pokemonId)
                
                // Gerçek API/DB güncelleme - önce backend'i güncelle
                val newIsFavorite = try {
                    val result = toggleFavoritePokemonUseCase(pokemonId)
                    // Bu useCase bir Flow döndürüyor, son değeri al
                    var success = false
                    result.collect { resource ->
                        if (!resource.isLoading) {
                            success = resource.data ?: oldIsFavorite
                        }
                    }
                    success
                } catch (e: Exception) {
                    _uiState.value = PokemonListUiState.Error("Favori durumu güncellenirken hata oluştu: ${e.localizedMessage}")
                    return@launch
                }
                
                // Backend güncellemesi başarılı olduktan sonra UI güncelle
                val updatedFavorites = if (newIsFavorite) {
                    _state.value.favoriteIds + pokemonId
                } else {
                    _state.value.favoriteIds - pokemonId
                }
                
                _state.update { it.copy(favoriteIds = updatedFavorites) }
                updateUiState()
                
            } catch (e: Exception) {
                // Hata durumunda kullanıcıya bildir
                _uiState.value = PokemonListUiState.Error("Favori durumu güncellenirken hata oluştu: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Pokemon domain modelini UI modeline dönüştür
     */
    private fun Pokemon.toUiModel(): PokemonItemUiModel {
        val isFavorite = _state.value.favoriteIds.contains(this.id)
        return PokemonItemUiModel(
            id = this.id,
            name = this.name,
            imageUrl = this.imageUrl,
            types = this.types,
            isFavorite = isFavorite
        )
    }

    /**
     * Favorileri gözlemle
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoritePokemonsUseCase().collectLatest { favoriteIds ->
                _state.update { it.copy(favoriteIds = favoriteIds) }
                // Favori değişimi varsa UI'ı güncelle
                if (_uiState.value is PokemonListUiState.Success) {
                    updateUiState()
                }
            }
        }
    }
    
    /**
     * Pokemon tiplerini API'den yükle
     */
    private fun updateAvailableTypesFromApi() {
        viewModelScope.launch {
            println("### Tip Listesi: Pokemon tipleri API'den alınıyor (API çağrısı başladı)")
            
            try {
                getPokemonTypesUseCase().collect { result ->
                    println("### Tip Listesi: API yanıtı alındı - durum: ${result.javaClass.simpleName}")
                    
                    when (result) {
                        is Resource.Success -> {
                            val types = result.data?.map { it.lowercase() }?.sorted() ?: emptyList()
                            println("### Tip Listesi: API'den ${types.size} tip alındı: $types")
                            
                            if (types.isNotEmpty()) {
                                _availableTypes.value = types
                                println("### Tip Listesi: availableTypes güncellendi: ${_availableTypes.value}")
                            } else {
                                updateAvailableTypesFromPokemons()
                            }
                        }
                        is Resource.Error -> {
                            println("### Tip Listesi: API hatası: ${result.message}")
                            updateAvailableTypesFromPokemons()
                        }
                        is Resource.Empty -> {
                            println("### Tip Listesi: API boş sonuç döndürdü")
                            updateAvailableTypesFromPokemons()
                        }
                        else -> {
                            // Loading durumunda bir şey yapma
                        }
                    }
                }
            } catch (e: Exception) {
                println("### Tip Listesi: API çağrısında beklenmeyen hata: ${e.message}")
                e.printStackTrace()
                updateAvailableTypesFromPokemons()
            }
        }
    }
    
    /**
     * Mevcut Pokemon verilerinden tip listesini oluştur
     */
    private fun updateAvailableTypesFromPokemons() {
        viewModelScope.launch {
            val allTypes = _state.value.pokemons
                .flatMap { it.types }
                .distinct()
                .sorted()
            
            println("### Tip Listesi: Mevcut Pokemon verilerinden ${allTypes.size} tip oluşturuldu: $allTypes")
            
            // Mevcut liste ile yeni liste aynı değilse güncelle
            if (_availableTypes.value != allTypes) {
                _availableTypes.value = allTypes
                println("### Tip Listesi: Tiplerin listesi güncellendi. Yeni liste: ${allTypes}")
            } else {
                println("### Tip Listesi: Mevcut Pokemon verileri zaten güncel, güncelleme yapılmadı")
            }
        }
    }
    
    /**
     * Pokemon verilerinden tip listesi oluştur
     */
    private fun updateAvailableTypes() {
        viewModelScope.launch {
            // Her durumda önce mevcut Pokemon verilerinden tipleri güncelle
            updateAvailableTypesFromPokemons()
            
            // Eğer hiç tip yoksa, API'den almayı dene
            if (_availableTypes.value.isEmpty()) {
                println("### Tip Listesi: Mevcut verilerden tip alınamadı, API'den almaya çalışılıyor")
                updateAvailableTypesFromApi()
            }
        }
    }
} 