package com.soulbuster.newsapp.util

sealed class Resoource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resoource<T>(data)
    class Error<T>(message: String,data: T? = null) : Resoource<T>(data,message)
    class Loading<T> : Resoource<T>()
}