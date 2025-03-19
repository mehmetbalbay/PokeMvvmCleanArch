package com.example.pokemonapp.core.common

/**
 * UI için Resource durum sınıfı
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val exception: Throwable, val message: String = exception.localizedMessage ?: "Bilinmeyen hata") : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
    data object Empty : Resource<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    val isEmpty: Boolean get() = this is Empty

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error, is Loading, is Empty -> null
    }

    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)
        fun error(exception: Throwable): Resource<Nothing> = Error(exception)
        fun loading(): Resource<Nothing> = Loading
        fun empty(): Resource<Nothing> = Empty
    }
} 