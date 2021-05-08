package com.moviedb.model.response.discover

import com.google.gson.annotations.SerializedName

data class DiscoverMoviesResponse(
    @SerializedName("results")
    val results : ArrayList<DiscoverMovies>
)