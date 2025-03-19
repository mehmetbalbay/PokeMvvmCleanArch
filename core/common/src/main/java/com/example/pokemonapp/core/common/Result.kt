package com.example.pokemonapp.core.common

/**
 * Ağ çağrıları ve veritabanı işlemleri için result wrapper
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error, is Loading -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        is Success, is Loading -> null
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Throwable): Result<Nothing> = Error(exception)
        fun loading(): Result<Nothing> = Loading
    }
} 