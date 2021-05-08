package com.moviedb.model.response.search

import com.google.gson.annotations.SerializedName

data class SearchMovie(
    @SerializedName("id")
    val id: String,
    @SerializedName("poster_path")
    val posterPath: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("vote_average")
    val score: Double
)