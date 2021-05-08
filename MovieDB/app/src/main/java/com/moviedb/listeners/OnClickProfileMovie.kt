package com.moviedb.listeners

import com.moviedb.model.repositories.FirebaseMovie

interface OnClickProfileMovie {
    fun onClick(firebaseMovie: FirebaseMovie)
}