package com.moviedb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.moviedb.R
import com.moviedb.listeners.OnClickItemSearch
import com.moviedb.model.response.search.SearchMovie
import com.moviedb.utils.Constants

class SearchAdapter() : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    var list: MutableList<SearchMovie> = mutableListOf()
    var onClickItemSearch: OnClickItemSearch? = null

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view){

        var poster : ImageView = view.findViewById(R.id.poster)
        var title : TextView = view.findViewById(R.id.title)
        var score : TextView = view.findViewById(R.id.score)
        var scoreStars : ImageView = view.findViewById(R.id.score_stars)

        fun bind(searchMovie: SearchMovie){

            itemView.setOnClickListener {
                onClickItemSearch?.onClick(searchMovie)
            }

            Glide.with(itemView)
                .load("${Constants.POSTER_IMAGE}${searchMovie.posterPath}")
                .placeholder(R.drawable.placeholder_transparent)
                .error(R.drawable.poster_not_available)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(poster)

            title.text = searchMovie.title
            score.text = String.format("%.1f", searchMovie.score)
            scoreStars.background.level = ((searchMovie.score.toDouble() * 1000) + 700).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setDataSearch(data: List<SearchMovie>){
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }
}