package com.example.pokemonapp.core.common.extensions

/**
 * String'in ilk harfini büyük yapar
 */
fun String.capitalizeFirst(): String {
    if (this.isEmpty()) return this
    return this.first().uppercase() + this.substring(1)
}

/**
 * String'i ID formatına dönüştürür (#001 gibi)
 */
fun Int.toIdString(digits: Int = 3): String {
    return "#${this.toString().padStart(digits, '0')}"
}

/**
 * Kilogram değerini daha okunabilir biçime dönüştürür
 */
fun Double.toWeightString(): String {
    return "$this kg"
}

/**
 * Metre değerini daha okunabilir biçime dönüştürür
 */
fun Double.toHeightString(): String {
    return "$this m"
} 