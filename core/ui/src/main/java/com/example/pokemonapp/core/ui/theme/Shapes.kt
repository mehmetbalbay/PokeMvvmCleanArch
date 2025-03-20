package com.example.pokemonapp.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Pokémon uygulaması için daha yuvarlak köşeli şekiller
val Shapes = Shapes(
    // Küçük bileşenler (örn. Button)
    small = RoundedCornerShape(12.dp),
    
    // Orta boyutlu bileşenler (örn. Card)
    medium = RoundedCornerShape(16.dp),
    
    // Büyük bileşenler (örn. ModalBottomSheet)
    large = RoundedCornerShape(24.dp),
    
    // Ekstra büyük bileşenler
    extraLarge = RoundedCornerShape(32.dp)
) 