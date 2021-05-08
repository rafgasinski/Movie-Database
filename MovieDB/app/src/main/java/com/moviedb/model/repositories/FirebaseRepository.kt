package com.moviedb.model.repositories

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseRepository {

    val databaseMessage = MutableLiveData<Event<String>>()
    var addingMovieToWatchedList = false
    val wasMovieAddedSuccessfully = MutableLiveData<Event<Boolean>>()
    val wasMovieWatched = MutableLiveData<Event<String>>()

    companion object
    {
        private val mAuth = FirebaseAuth.getInstance()
        private val currentUser = mAuth.currentUser

        private val userMovieData = Firebase.database.reference

        private fun getUserAllMovies(userId: String) = userMovieData.child(userId)
    }

    fun getMovie(movieId: String) {
        getUserAllMovies(currentUser!!.uid).child(movieId).get().addOnSuccessListener {
            wasMovieWatched.value = Event(it.child("watched").value.toString())
        }
    }

    fun addMovie(movie: FirebaseMovie) {
        getUserAllMovies(currentUser!!.uid).child(movie.id).setValue(movie).addOnSuccessListener {
            if (!movie.watched) {
                databaseMessage.value = Event("Added: '${movie.title}' to watchlist")
            } else {
                databaseMessage.value = Event("Added '${movie.title}' to watched list")
            }
            wasMovieAddedSuccessfully.value = Event(true)
        }.addOnFailureListener {
            addingMovieToWatchedList = movie.watched != false

            databaseMessage.value = Event("Error: Movie was not added")
            wasMovieAddedSuccessfully.value = Event(false)
        }
    }

    fun removeMovie(movieId : String) {
        getUserAllMovies(currentUser!!.uid).child(movieId).removeValue()
    }

}

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandledOrReturnNull(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}