package com.soulbuster.newsapp.models

import com.soulbuster.newsapp.models.Article

data class NewsResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)