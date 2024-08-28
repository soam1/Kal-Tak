package com.akashsoam.kaltak.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akashsoam.kaltak.models.Article
import com.akashsoam.kaltak.models.NewsResponse
import com.akashsoam.kaltak.repository.NewsRepository
import com.akashsoam.kaltak.util.Resource
import kotlinx.coroutines.launch

class NewsViewModel(app: Application, private val newsRepository: NewsRepository) :
    AndroidViewModel(app) {
    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesResponse: NewsResponse? = null


    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    init {
        getHeadlines("in")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }


    private fun handleHeadlinesResponse(response: retrofit2.Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinesPage++
                if (headlinesResponse == null) {
                    headlinesResponse = resultResponse
                } else {
                    val oldArticles = headlinesResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    //    private fun handleSearchNewsResponse(response: retrofit2.Response<NewsResponse>): Resource<NewsResponse> {
//        if (response.isSuccessful) {
//            response.body()?.let { resultResponse ->
//
//                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
//                    searchNewsPage = 1
//                    searchNewsResponse = resultResponse
//                    oldSearchQuery = newSearchQuery
//                } else {
//                    searchNewsPage++
//                    val oldArticles = searchNewsResponse?.articles
//                    val newArticles = resultResponse.articles
//                    oldArticles?.addAll(newArticles)
//                }
//                return Resource.Success(searchNewsResponse ?: resultResponse)
//            }
//        }
//        return Resource.Error(response.message())
//    }
    private fun handleSearchNewsResponse(response: retrofit2.Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse =
                        resultResponse //if first page save the result to the response
                } else {
                    val oldArticles = searchNewsResponse?.articles //else, add all articles to old
                    val newArticle = resultResponse.articles //add new response to new
                    oldArticles?.addAll(newArticle) //add new articles to old articles
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToFavourites(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteFromFavourites(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    //    fun internetConnection(context: Context): Boolean {
//        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager).apply {
//            return getNetworkCapabilities(activeNetwork)?.run {
//                when {
//                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
//                    else -> false
//                }
//            } ?: false
//        }
//    }
    fun internetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private suspend fun headlinesInternet(countryCode: String) {
        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(getApplication())) {
                val response = newsRepository.getHeadlines(countryCode, headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is java.net.UnknownHostException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery: String) {
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(getApplication())) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is java.net.UnknownHostException -> searchNews.postValue(Resource.Error("Unable to connect"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

}