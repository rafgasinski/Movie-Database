package com.moviedb.model.response.home

import com.google.gson.annotations.SerializedName

data class HomeMovieResponse(
    @SerializedName("results")
    val results : ArrayList<HomeMovie>
)