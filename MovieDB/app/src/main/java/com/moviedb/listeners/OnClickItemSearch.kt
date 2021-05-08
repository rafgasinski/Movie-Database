package com.moviedb.listeners

import com.moviedb.model.response.search.SearchMovie

interface OnClickItemSearch {
    fun onClick(searchMovie: SearchMovie)
}