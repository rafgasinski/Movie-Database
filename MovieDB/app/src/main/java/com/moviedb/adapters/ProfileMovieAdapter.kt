package com.moviedb.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.moviedb.R
import com.moviedb.listeners.OnClickProfileMovie
import com.moviedb.model.repositories.FirebaseMovie
import com.moviedb.utils.Constants
import com.moviedb.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProfileMovieAdapter(var viewModel: ProfileViewModel): RecyclerView.Adapter<ProfileMovieAdapter.ProfileMovieViewHolder>(), Filterable {

    var listAllMovies : ArrayList<FirebaseMovie> = arrayListOf()
    var listSearchedMovies : ArrayList<FirebaseMovie> = arrayListOf()
    var onClickHomeListener: OnClickProfileMovie? = null
    val sdf = SimpleDateFormat("yyyy/M/d HH:mm:ss")

    inner class ProfileMovieViewHolder(view: View) : RecyclerView.ViewHolder(view){

        var backdrop : ImageView = view.findViewById(R.id.backdrop)
        var backdropCard : CardView = view.findViewById(R.id.backdrop_cardview)
        var title : TextView = view.findViewById(R.id.title)
        var overview : TextView = view.findViewById(R.id.overview)
        var score : TextView = view.findViewById(R.id.score)
        var scoreStars : ImageView = view.findViewById(R.id.score_stars)

        var watchedIcon : ImageButton = view.findViewById(R.id.watched_icon)
        var toWatchIcon : ImageButton = view.findViewById(R.id.to_watch_icon)


        @SuppressLint("Range")
        fun bind(firebaseMovie: FirebaseMovie){
            itemView.setOnClickListener {
                onClickHomeListener?.onClick(firebaseMovie)
            }

            Glide.with(itemView)
                    .load("${Constants.BACKDROP_IMAGE}${firebaseMovie.backdropPath}")
                    .placeholder(R.drawable.placeholder_transparent)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            /*val animation = AnimationUtils.loadAnimation(backdrop.context, R.anim.fadeout)
                            backdropCard.startAnimation(animation)*/
                            backdropCard.visibility = View.GONE
                            return true
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            /*val animation = AnimationUtils.loadAnimation(backdrop.context, R.anim.fadein)
                            backdropCard.startAnimation(animation)*/
                            backdropCard.visibility = View.VISIBLE
                            return false
                        }

                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(backdrop)

            title.text = firebaseMovie.title

            if(!firebaseMovie.overview.isNullOrEmpty()){
                overview.visibility = View.VISIBLE
                overview.text = firebaseMovie.overview
            } else {
                overview.visibility = View.GONE
            }

            score.text = firebaseMovie.score
            scoreStars.background.level = ((firebaseMovie.score.toDouble() * 1000) + 700).toInt()

            if(firebaseMovie.watched){
                toWatchIcon.setImageResource(R.drawable.ic_to_watch)
                watchedIcon.setImageResource(R.drawable.ic_watched_remove)

                toWatchIcon.setOnClickListener{
                    firebaseMovie.watched = false
                    firebaseMovie.additionDate = sdf.format(Date())
                    viewModel.firebaseRepository.addMovie(firebaseMovie)
                }

                watchedIcon.setOnClickListener {
                    listSearchedMovies.remove(firebaseMovie)
                    listAllMovies.remove(firebaseMovie)
                    notifyItemRemoved(listSearchedMovies.indexOf(firebaseMovie))

                    viewModel.firebaseRepository.removeMovie(firebaseMovie.id)
                    Toast.makeText(watchedIcon.context, "Removed: '${firebaseMovie.title}' from watched", Toast.LENGTH_SHORT).show()
                }
            } else {
                toWatchIcon.setImageResource(R.drawable.ic_to_watch_filled)
                watchedIcon.setImageResource(R.drawable.ic_watched)

                toWatchIcon.setOnClickListener {
                    listSearchedMovies.remove(firebaseMovie)
                    listAllMovies.remove(firebaseMovie)
                    notifyItemRemoved(listSearchedMovies.indexOf(firebaseMovie))

                    viewModel.firebaseRepository.removeMovie(firebaseMovie.id)
                    Toast.makeText(toWatchIcon.context, "Removed '${firebaseMovie.title}' from watchlist", Toast.LENGTH_SHORT).show()
                }

                watchedIcon.setOnClickListener{
                    firebaseMovie.watched = true
                    firebaseMovie.additionDate = sdf.format(Date())
                    viewModel.firebaseRepository.addMovie(firebaseMovie)
                }
            }

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileMovieViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_movie, parent, false)
        return ProfileMovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileMovieViewHolder, position: Int) {
        holder.bind(listSearchedMovies[position])
    }

    override fun getItemCount(): Int {
        return listSearchedMovies.size
    }

    override fun getFilter(): Filter {
        return  filterSearch
    }

    private var filterSearch  = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList : ArrayList<FirebaseMovie> = arrayListOf()

            if(constraint.toString().isEmpty()){
                Log.d("constraintEmpty", "lol")
                filteredList.addAll(listAllMovies)
            } else {
                for (movie in listAllMovies) {
                    if(movie.title.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(movie)
                    }
                }
            }

            var filterResults = FilterResults()
            filterResults.values = filteredList

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            listSearchedMovies = arrayListOf()
            listSearchedMovies = results?.values as ArrayList<FirebaseMovie>
            notifyDataSetChanged()
        }

    }

    fun setData(data: ArrayList<FirebaseMovie>) {
        listAllMovies = data
        listSearchedMovies = data
        notifyDataSetChanged()
    }
}