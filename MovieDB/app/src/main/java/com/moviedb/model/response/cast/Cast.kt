package com.moviedb.model.response.cast

import com.google.gson.annotations.SerializedName

data class Cast(
    @SerializedName("id")
    val id: String,
    @SerializedName("original_name")
    val name: String,
    @SerializedName("profile_path")
    val profile: String
)