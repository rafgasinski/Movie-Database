package com.moviedb.model.response.movie

import com.google.gson.annotations.SerializedName

data class MovieVideo(
        @SerializedName("id")
        var id : String,
        @SerializedName("name")
        var name : String,
        @SerializedName("key")
        var key : String,
        @SerializedName("size")
        var size : String
)