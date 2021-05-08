package com.moviedb.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.moviedb.R
import com.moviedb.listeners.OnClickHomeMovie
import com.moviedb.model.response.home.HomeMovie
import com.moviedb.utils.Constants

class HomeMovieAdapter(): RecyclerView.Adapter<HomeMovieAdapter.HomeViewHolder>() {

    var list : MutableList<HomeMovie> = mutableListOf()
    var onClickHomeListener: OnClickHomeMovie? = null

    inner class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view){

        var poster : ImageView = view.findViewById(R.id.poster)
        var title : TextView = view.findViewById(R.id.title)
        var score : TextView = view.findViewById(R.id.score)
        var scoreStars : ImageView = view.findViewById(R.id.score_stars)

        @SuppressLint("Range")
        fun bind(homeMovie: HomeMovie){
            itemView.setOnClickListener {
                onClickHomeListener?.onClick(homeMovie)
            }

            Glide.with(itemView)
                .load("${Constants.POSTER_IMAGE}${homeMovie.posterPath}")
                .placeholder(R.drawable.placeholder_transparent)
                .error(R.drawable.poster_not_available)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(poster)

            title.text = homeMovie.title
            score.text = String.format("%.1f", homeMovie.score)
            scoreStars.background.level = ((homeMovie.score * 1000) + 700).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setDataHomeMovies(data: List<HomeMovie>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }
}