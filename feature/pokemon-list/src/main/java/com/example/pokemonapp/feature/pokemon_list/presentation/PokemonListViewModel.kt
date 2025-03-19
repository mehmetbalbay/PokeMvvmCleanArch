package com.example.pokemonapp.feature.pokemon_list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonapp.feature.pokemon_list.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonListState())
    val state: StateFlow<PokemonListState> = _state.asStateFlow()
    
    // Tip listesi için ayrı state
    private val _availableTypes = MutableStateFlow<List<String>>(emptyList())
    val availableTypes: StateFlow<List<String>> = _availableTypes.asStateFlow()
    
    // Sayfalama için değişkenler
    private var currentPage = 0
    private val pageSize = 20
    private var hasMoreData = true
    private var isLoading = false
    
    private var favoriteIds = setOf<Int>()

    init {
        loadPokemons()
        observeFavorites()
    }
    
    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeAllFavorites().collectLatest { favoriteIdList ->
                val newFavoriteIds = favoriteIdList.toSet()
                
                // Eğer favorilerde değişiklik yoksa erken çıkış
                if (newFavoriteIds == favoriteIds) return@collectLatest
                
                favoriteIds = newFavoriteIds
                updatePokemonFavoriteStatus()
            }
        }
    }
    
    private fun updatePokemonFavoriteStatus() {
        val currentPokemons = _state.value.pokemons
        if (currentPokemons.isEmpty()) return
        
        val updatedPokemons = currentPokemons.map { pokemon ->
            val isFavorite = favoriteIds.contains(pokemon.id)
            if (pokemon.isFavorite != isFavorite) {
                pokemon.copy(isFavorite = isFavorite)
            } else {
                pokemon
            }
        }
        
        if (updatedPokemons != currentPokemons) {
            _state.update { it.copy(pokemons = updatedPokemons) }
        }
    }
    
    // Pokemon verilerinden tip listesi oluştur
    private fun updateAvailableTypes() {
        val allTypes = _state.value.pokemons
            .flatMap { it.types }
            .distinct()
            .sorted()
        
        _availableTypes.value = allTypes
    }

    fun loadPokemons() {
        // İşlem zaten devam ediyorsa veya daha fazla veri yoksa çık
        if (isLoading || !hasMoreData) {
            return
        }
        
        // Herhangi bir filtreleme aktifse, yeni veri yükleme
        val currentState = _state.value
        if (currentState.searchQuery.isNotEmpty() || 
            currentState.selectedType != null || 
            currentState.showFavoritesOnly) {
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Verileri çek
                val response = repository.getPokemonsWithCount(currentPage * pageSize, pageSize)
                val newPokemons = response.pokemons
                val totalCount = response.count
                
                // Eğer yeni veri yoksa, daha fazla veri olmadığını işaretle ve çık
                if (newPokemons.isEmpty()) {
                    hasMoreData = false
                    if (_state.value.pokemons.isEmpty()) {
                        _state.update { it.copy(isLoading = false) }
                    }
                    return@launch
                }
                
                // Pokemonları güncel favori durumlarıyla eşleştir
                val updatedNewPokemons = newPokemons.map { pokemon ->
                    pokemon.copy(isFavorite = favoriteIds.contains(pokemon.id))
                }
                
                // Mevcut verileri al
                val currentPokemons = _state.value.pokemons
                
                // Yeni verileri listeye ekle
                val combinedPokemons = currentPokemons + updatedNewPokemons
                
                // Toplam sayı ile karşılaştırarak daha fazla veri olup olmadığını kontrol et
                val nextOffset = (currentPage + 1) * pageSize
                hasMoreData = nextOffset < totalCount
                
                // Sayfa sayısını artır
                currentPage++
                
                // UI'ı güncelle
                _state.update { it.copy(pokemons = combinedPokemons, isLoading = false) }
                
                // Tip listesini güncelle
                updateAvailableTypes()
                
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Bilinmeyen bir hata oluştu", isLoading = false) }
            } finally {
                isLoading = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        // Eğer daha önce bir arama yokken yeni bir arama başlatılıyorsa,
        // arama yaparken sayfalama durumunu askıya al
        val isNewSearch = _state.value.searchQuery.isEmpty() && query.isNotEmpty()
        
        _state.update { it.copy(searchQuery = query) }
        
        if (isNewSearch) {
            // Mevcut filtrelenmiş listeyi oluştur ve göster
            applyFilters()
        } else if (query.isEmpty()) {
            // Arama temizlendiğinde mevcut listeyi göster, sayfalama devam edebilir
            _state.update { it.copy(
                pokemons = _state.value.pokemons
            ) }
        } else {
            // Arama değiştiğinde mevcut filtrelenmiş listeyi güncelle
            applyFilters()
        }
    }

    fun updateSelectedType(type: String?) {
        _state.update { it.copy(selectedType = type) }
        
        // Tip değiştiğinde mevcut listeyi filtrele, yeni veri çekme
        applyFilters()
    }

    fun toggleFavoritesOnly() {
        _state.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
        
        // Favorileri değiştirince mevcut listeyi filtrele, yeni veri çekme
        applyFilters()
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        _state.update { it.copy(sortOrder = sortOrder) }
        
        // Sıralama değiştiğinde mevcut listeyi filtrele, yeni veri çekme
        applyFilters()
    }
    
    // Mevcut listedeki pokemonları filtrele ve göster
    private fun applyFilters() {
        // Filtre işlemini UI thread'inde değil, coroutine içinde yapalım
        viewModelScope.launch {
            // Filtreleme zaten Screen tarafında yapılıyor olsa da, burada da yapabiliriz
            // Bu şekilde, ViewModel seviyesinde filtre kontrolleri de olur
            // Ancak bu projede, filtreleme/sıralama işlemleri ekran tarafında yapıldığı için
            // bu fonksiyon şu an için bir şey yapmıyor.
            // İleride bu fonksiyonu genişletip, filtreleme mantığını ViewModel'e taşıyabilirsiniz.
            
            // Not: Değişiklik yapmadan state'i güncellemek, gereksiz render'lara neden olabilir
            // Bu nedenle işlem yapmıyoruz.
        }
    }

    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            val isFavorite = repository.toggleFavorite(pokemonId)
            // Not: Artık favori değişiklikleri observeFavorites() metoduyla izleniyor
            // Ancak UI'ı hemen güncellemek için burada da düzenleme yapıyoruz
            _state.update { currentState ->
                currentState.copy(
                    pokemons = currentState.pokemons.map { pokemon ->
                        if (pokemon.id == pokemonId) {
                            pokemon.copy(isFavorite = isFavorite)
                        } else {
                            pokemon
                        }
                    }
                )
            }
        }
    }

    fun refresh() {
        currentPage = 0
        hasMoreData = true
        _state.update { it.copy(pokemons = emptyList(), isLoading = true) }
        loadPokemons()
    }
    
    fun loadMorePokemons() {
        loadPokemons()
    }
} 