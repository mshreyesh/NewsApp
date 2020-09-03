package com.soulbuster.newsapp.repository

import com.soulbuster.newsapp.api.RetrofitInstance
import com.soulbuster.newsapp.db.ArticleDatabase
import com.soulbuster.newsapp.models.Article

class NewsRepository(
    val db: ArticleDatabase
) {
    suspend fun getBreakingNews(countryCode: String,pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)

    suspend fun searchNews(searchQuery: String,pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)

    suspend fun upsert(article: Article) = db.getArticledao().upsert(article)

    fun getSavedNews() = db.getArticledao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticledao().deleteArticle(article)
}