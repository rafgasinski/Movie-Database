package com.moviedb.model.response.movie

import com.google.gson.annotations.SerializedName

data class MovieVideosResponse(
        @SerializedName("results")
        var videos: List<MovieVideo>?
)
