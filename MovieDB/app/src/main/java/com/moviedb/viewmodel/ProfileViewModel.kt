package com.moviedb.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.moviedb.model.repositories.FirebaseMovie
import com.moviedb.model.repositories.FirebaseRepository

class ProfileViewModel : ViewModel() {
    val firebaseRepository = FirebaseRepository()

    var moviesToWatch: LiveData<ArrayList<FirebaseMovie>> = liveData { mutableListOf<FirebaseMovie>() }

}