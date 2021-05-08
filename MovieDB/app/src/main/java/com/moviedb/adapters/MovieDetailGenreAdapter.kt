package com.moviedb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moviedb.R

class MovieDetailGenreAdapter : RecyclerView.Adapter<MovieDetailGenreAdapter.MovieDetailGenreViewHolder>() {

    private var list: MutableList<String> = mutableListOf()

    inner class MovieDetailGenreViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var genreTextView: TextView = view.findViewById(R.id.movie_detail_genre)

        fun bind(genre: String) {
            genreTextView.text = genre
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieDetailGenreViewHolder {
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_genre, parent, false)
        return MovieDetailGenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieDetailGenreViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setDataMovieGenres(data: List<String>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }
}