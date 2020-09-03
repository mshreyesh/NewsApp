package com.soulbuster.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soulbuster.newsapp.NewsApplication
import com.soulbuster.newsapp.models.Article
import com.soulbuster.newsapp.models.NewsResponse
import com.soulbuster.newsapp.repository.NewsRepository
import com.soulbuster.newsapp.util.Resoource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(
    app: Application,
    val newsRepository: NewsRepository
) : AndroidViewModel(app) {

    val breakingNews : MutableLiveData<Resoource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse? = null

    val searchcNews : MutableLiveData<Resoource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode)

    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>) : Resoource<NewsResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse=resultResponse
                } else{
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return  Resoource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resoource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>) : Resoource<NewsResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse=resultResponse
                } else{
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return  Resoource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resoource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        searchcNews.postValue(Resoource.Loading())
        try {
            if (hasInternet()){
                val response = newsRepository.searchNews(searchQuery,searchNewsPage)
                searchcNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchcNews.postValue(Resoource.Error("No Internet Connectivity"))
            }
        } catch (t: Throwable) {
            when(t){
                is IOException -> searchcNews.postValue(Resoource.Error("Network Failure"))
                else -> searchcNews.postValue(Resoource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resoource.Loading())
        try {
            if (hasInternet()){
                val response = newsRepository.getBreakingNews(countryCode,breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resoource.Error("No Internet Connectivity"))
            }
        } catch (t: Throwable) {
            when(t){
                is IOException -> breakingNews.postValue(Resoource.Error("Network Failure"))
                else -> breakingNews.postValue(Resoource.Error("Conversion Error"))
            }
        }
    }

    private fun hasInternet(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when(type) {
                    TYPE_WIFI -> true
                    TYPE_ETHERNET -> true
                    TYPE_MOBILE -> true
                    else -> false
                }
            }
        }
        return false
    }
}