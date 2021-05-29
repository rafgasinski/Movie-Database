package com.moviedb.model.response.cast

import com.google.gson.annotations.SerializedName

data class CastDetails (
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("birthday")
        val birthday: String,
        @SerializedName("deathday")
        val deathday: String,
        @SerializedName("biography")
        val biography: String,
        @SerializedName("place_of_birth")
        val placeOfBirth: String,
        @SerializedName("profile_path")
        val profilePath: String
)