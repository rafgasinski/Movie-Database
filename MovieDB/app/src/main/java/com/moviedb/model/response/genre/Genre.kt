package com.moviedb.model.response.genre

import com.google.gson.annotations.SerializedName

class Genre(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val nameGenre: String
)