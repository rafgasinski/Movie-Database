package com.moviedb.listeners

import com.moviedb.model.response.home.HomeMovie

interface OnClickHomeMovie {
    fun onClick(homeMovie: HomeMovie)
}