package com.moviedb.listeners

import com.moviedb.model.response.movie.MovieVideo

interface OnClickVideoTrailer {
    fun onClick(movieVideo: MovieVideo)
}