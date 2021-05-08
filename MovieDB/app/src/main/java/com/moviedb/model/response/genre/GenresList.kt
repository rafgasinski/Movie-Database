package com.moviedb.model.response.genre

import com.google.gson.annotations.SerializedName


data class GenresList(
    @SerializedName("genres")
    val genres: List<Genre>
)