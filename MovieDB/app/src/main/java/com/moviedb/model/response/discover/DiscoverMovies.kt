package com.moviedb.model.response.discover

import com.google.gson.annotations.SerializedName

data class DiscoverMovies(
    @SerializedName("id")
    val id : String,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("original_title")
    val original_title: String,
    @SerializedName("title")
    val title: String
)