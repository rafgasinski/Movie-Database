package com.moviedb.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.moviedb.R
import com.moviedb.listeners.OnClickVideoTrailer
import com.moviedb.model.response.movie.MovieVideo


class VideosAdapter(var trailersInfo: TextView) : RecyclerView.Adapter<VideosAdapter.CastViewHolder>() {

    var list: MutableList<MovieVideo> = mutableListOf()
    var onClickListener: OnClickVideoTrailer? = null

    inner class CastViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var trailerPoster: ImageView = view.findViewById(R.id.trailer_poster_image)

        fun bind(video: MovieVideo) {
            itemView.setOnClickListener {
                onClickListener?.onClick(video)
            }


            Glide.with(itemView)
                    .load("https://img.youtube.com/vi/${video.key}/0.jpg")
                    .placeholder(R.drawable.placeholder_transparent)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            list.remove(video)
                            if(list.size == 0){
                                trailersInfo.visibility = View.INVISIBLE
                            }
                            notifyDataSetChanged()
                            return true
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                    })
                    .thumbnail(0.05f)
                    .transition(withCrossFade())
                    .centerCrop()
                    .into(trailerPoster)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_trailer, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setDataCast(data: List<MovieVideo>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }
}