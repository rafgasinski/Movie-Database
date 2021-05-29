package com.moviedb.listeners

import com.moviedb.model.response.cast.Cast
import com.moviedb.model.response.home.HomeMovie

interface OnClickCastItem {
    fun onClick(cast: Cast)
}