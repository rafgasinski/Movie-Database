package com.moviedb.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviedb.model.repositories.ApiRepository
import com.moviedb.model.repositories.FirebaseRepository
import com.moviedb.model.response.cast.Cast
import com.moviedb.model.response.movie.DetailResponse
import com.moviedb.model.response.movie.MovieVideosResponse
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val apiRepository = ApiRepository()
    val firebaseRepository = FirebaseRepository()

    /**
     * Data response lists
     * */
    private val _detailResponse = MutableLiveData<DetailResponse>()
    val detailResponse: LiveData<DetailResponse> = _detailResponse

    private val _castResponseList = MutableLiveData<ArrayList<Cast>>()
    val castResponseList: LiveData<ArrayList<Cast>> = _castResponseList

    lateinit var videoResult: MovieVideosResponse

    /**
     * Functions to get adequate data from API
     * */
    fun getDetails(movieId : String) {
        viewModelScope.launch {
            val response = apiRepository.getDetailMovie(movieId)

            if(response.isSuccessful){
                val detailResponse = response.body()!!
                videoResult = detailResponse.videosResult!!
                _detailResponse.postValue(detailResponse)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }

    fun getCast(movieId: String) {
        viewModelScope.launch {
            val response = apiRepository.getCast(movieId)

            if(response.isSuccessful){
                val castResponse = response.body()!!
                _castResponseList.postValue(castResponse.cast)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }
}
