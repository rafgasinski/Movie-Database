package com.moviedb.model.response.movie

import com.google.gson.annotations.SerializedName
import com.moviedb.model.response.genre.Genre

data class DetailResponse(
        @SerializedName("backdrop_path")
        val backdropPath: String?,
        @SerializedName("genres")
        val genres: List<Genre>,
        @SerializedName("poster_path")
        val posterPath: String?,
        @SerializedName("id")
        val id: Int,
        @SerializedName("title")
        val title: String,
        @SerializedName("overview")
        val description: String,
        @SerializedName("popularity")
        val popularity: Double,
        @SerializedName("release_date")
        val releaseDate: String,
        @SerializedName("runtime")
        val duration: Int,
        @SerializedName("vote_average")
        val score: String,
        @SerializedName("videos")
        var videosResult: MovieVideosResponse?
)