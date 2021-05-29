package com.moviedb.model.response.home

import com.google.gson.annotations.SerializedName

data class MovieCreditsResponse(
        @SerializedName("cast")
        val cast : ArrayList<HomeMovie>
)