package com.moviedb.model.response.home

import com.google.gson.annotations.SerializedName

data class HomeMovie(
    @SerializedName("id")
    val id : String,
    @SerializedName("poster_path")
    val posterPath: String,
    @SerializedName("original_title")
    val original_title: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("vote_average")
    val score: Double
)