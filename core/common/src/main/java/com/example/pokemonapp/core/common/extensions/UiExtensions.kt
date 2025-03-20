package com.example.pokemonapp.core.common.extensions

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * LazyGridState için uzantı fonksiyonu. Grid'in en sonuna gelip gelmediğini state olarak döndürür.
 */
@Composable
fun LazyGridState.isScrolledToEndState(): State<Boolean> {
    return remember {
        derivedStateOf {
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.last()
                val viewportSize = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset
                
                (lastVisibleItem.index + 1 >= layoutInfo.totalItemsCount && 
                        lastVisibleItem.offset.y + lastVisibleItem.size.height <= viewportSize)
            }
        }
    }
}

/**
 * LazyGridState için uzantı fonksiyonu. Grid'in en sonuna gelip gelmediğini boolean olarak döndürür.
 */
@Composable
fun LazyGridState.isScrolledToEnd(): Boolean {
    return isScrolledToEndState().value
}

/**
 * Modifier için uzantı fonksiyonu. Görünürlük durumuna göre alpha değerini ayarlar.
 */
fun Modifier.visibleIf(isVisible: Boolean): Modifier = this.then(
    if (isVisible) Modifier else Modifier.alpha(0f)
)

/**
 * Bir rengin açık mı koyu mu olduğunu belirler. Koyu renklerde beyaz metin, 
 * açık renklerde siyah metin kullanılabilir.
 */
fun Color.isLight(): Boolean {
    val rgb = this.toArgb()
    val red = android.graphics.Color.red(rgb) / 255.0
    val green = android.graphics.Color.green(rgb) / 255.0
    val blue = android.graphics.Color.blue(rgb) / 255.0
    
    // YIQ renk uzayında parlaklık hesaplama formülü
    val yiq = ((red * 299) + (green * 587) + (blue * 114)) / 1000
    
    // 0.5'ten büyükse açık, küçükse koyu renk
    return yiq > 0.5
}

/**
 * Renk için kontrast değerini hesaplar ve metin rengi olarak siyah veya beyaz döndürür
 */
fun Color.contrastColor(): Color {
    return if (this.isLight()) Color.Black else Color.White
} 