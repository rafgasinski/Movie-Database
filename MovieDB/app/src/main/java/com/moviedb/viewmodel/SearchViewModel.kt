package com.moviedb.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviedb.model.repositories.ApiRepository
import com.moviedb.model.response.search.SearchMovie
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val apiRepository = ApiRepository()

    private val _responseSearchList = MutableLiveData<ArrayList<SearchMovie>>()
    val responseSearchList: LiveData<ArrayList<SearchMovie>> = _responseSearchList

    private val _responseRecommendedList = MutableLiveData<ArrayList<SearchMovie>>()
    val responseRecommendedList: LiveData<ArrayList<SearchMovie>> = _responseRecommendedList

    fun getSearchMovie(query : String) {
        viewModelScope.launch {
            val response = apiRepository.getSearchMovie(query)

            if(response.isSuccessful){
                val searchResponse = response.body()!!
                _responseSearchList.postValue(searchResponse.results)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }

    fun getRecommendations(movieId : String) {
        viewModelScope.launch {
            val response = apiRepository.getRecommendations(movieId)

            if(response.isSuccessful){
                val searchResponse = response.body()!!
                _responseRecommendedList.postValue(searchResponse.results)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }
}