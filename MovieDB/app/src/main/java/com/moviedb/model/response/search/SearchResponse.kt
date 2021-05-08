package com.moviedb.model.response.search

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("results")
    val results: ArrayList<SearchMovie>
)