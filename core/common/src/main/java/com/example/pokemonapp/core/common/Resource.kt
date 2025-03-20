package com.example.pokemonapp.core.common

/**
 * Network ve diğer verileri sarmak için kullanılan Resource sınıfı.
 * Bu, Clean Architecture'da domain katmanı tarafından kullanılır.
 *
 * @param T Wrap edilen veri tipi
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * Başarılı bir sonuç. Veriler mevcuttur.
     */
    class Success<T>(data: T, message: String? = null) : Resource<T>(data, message)

    /**
     * Yükleniyor durumu. Veriler henüz mevcut değildir.
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)

    /**
     * Hata durumu. Bir hata mesajı içerir.
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    
    /**
     * Boş durum. Veri yok.
     */
    class Empty<T>(message: String? = null, data: T? = null) : Resource<T>(data, message)

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
        fun error(exception: Throwable): Resource<Nothing> = Error(exception.localizedMessage ?: "Bilinmeyen hata")
        fun loading(): Resource<Nothing> = Loading()
        fun empty(): Resource<Nothing> = Empty()
    }
} 