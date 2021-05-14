package com.moviedb.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviedb.model.repositories.ApiRepository
import com.moviedb.model.response.discover.DiscoverMovies
import com.moviedb.model.response.genre.Genre
import com.moviedb.model.response.home.HomeMovie
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val apiRepository = ApiRepository()

    /**
     * Data response lists
     * */
    private val _trendingResponseList = MutableLiveData<ArrayList<HomeMovie>>()
    val homeResponseList: LiveData<ArrayList<HomeMovie>> = _trendingResponseList

    private val _discoverMoviesResponseList = MutableLiveData<ArrayList<DiscoverMovies>>()
    val discoverMoviesResponseList: LiveData<ArrayList<DiscoverMovies>> = _discoverMoviesResponseList

    private val _discoverGenreResponseList = MutableLiveData<ArrayList<HomeMovie>>()
    val discoverGenreResponseList: LiveData<ArrayList<HomeMovie>> = _discoverGenreResponseList

    private val _genreResponseList = MutableLiveData<List<Genre>>()
    val genreResponseList: LiveData<List<Genre>> = _genreResponseList

    /**
     * Functions to get adequate data from API
     * */
    fun getTrending() {
        viewModelScope.launch {
            val response = apiRepository.getTrending()

            if(response.isSuccessful){
                val trendingResponse = response.body()!!
                _trendingResponseList.postValue(trendingResponse.results)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }

    fun getDiscoverMovies() {
        viewModelScope.launch {
            val response = apiRepository.getDiscoverMovies()

            if(response.isSuccessful){
                val discoverResponse = response.body()!!
                _discoverMoviesResponseList.postValue(discoverResponse.results)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }

    fun getDiscoverGenre(genre : String) {
        viewModelScope.launch {
            val response = apiRepository.getDiscoverSelectedGenre(genre)

            if(response.isSuccessful){
                val discoverGenreResponse = response.body()!!
                _discoverGenreResponseList.postValue(discoverGenreResponse.results)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }

    fun getGenreList() {
        viewModelScope.launch {
            val response = apiRepository.getGenreList()
            if(response.isSuccessful){
                val genreListResponse = response.body()!!
                _genreResponseList.postValue(genreListResponse.genres)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
         }

    }
}