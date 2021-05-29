package com.moviedb.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviedb.model.repositories.ApiRepository
import com.moviedb.model.response.cast.Cast
import com.moviedb.model.response.cast.CastDetails
import com.moviedb.model.response.home.HomeMovie
import kotlinx.coroutines.launch

class CastViewModel : ViewModel() {

    private val apiRepository = ApiRepository()

    private val _personDetails = MutableLiveData<CastDetails>()
    val personDetails: LiveData<CastDetails> = _personDetails

    private val _movieCreditsResponse = MutableLiveData<ArrayList<HomeMovie>>()
    val movieCreditsResponse: LiveData<ArrayList<HomeMovie>> = _movieCreditsResponse

    fun getPersonDetails(personId : String) {
        viewModelScope.launch {
            val response = apiRepository.getPersonDetails(personId)

            if(response.isSuccessful){
                val personDetails = response.body()!!
                _personDetails.postValue(personDetails)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }

    fun getMovieCredits(personId : String) {
        viewModelScope.launch {
            val response = apiRepository.getMovieCredits(personId)

            if(response.isSuccessful){
                val movieCredits = response.body()!!
                _movieCreditsResponse.postValue(movieCredits.cast)
            }
            else{
                Log.d("Response", response.errorBody().toString())
                Log.d("ErrorCode:", response.code().toString())
            }
        }
    }
}