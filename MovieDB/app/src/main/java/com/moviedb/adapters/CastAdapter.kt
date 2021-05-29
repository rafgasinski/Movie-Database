package com.moviedb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moviedb.R
import com.moviedb.listeners.OnClickCastItem
import com.moviedb.model.response.cast.Cast
import com.moviedb.utils.Constants

class CastAdapter(var castTextInfo: TextView) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    private var list: MutableList<Cast> = mutableListOf()
    var onClickCastItem: OnClickCastItem? = null

    inner class CastViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var castPhoto: ImageView = view.findViewById(R.id.cast_photo)
        var castFirstname: TextView = view.findViewById(R.id.cast_firstName)
        var castSurname: TextView = view.findViewById(R.id.cast_surname)

        fun bind(cast: Cast) {
            itemView.setOnClickListener {
                onClickCastItem?.onClick(cast)
            }

            Glide.with(itemView)
                    .load("${Constants.CAST_IMAGE}${cast.profile}")
                    .placeholder(R.drawable.placeholder_transparent)
                    .error(R.drawable.cast_error)
                    .into(castPhoto)

            val fullName = cast.name.split(" ")

            when(fullName.count()){
                1 -> castFirstname.text = cast.name
                in 4..fullName.count() -> {
                    castSurname.text = fullName.takeLast(2).joinToString(separator = " ")
                    castFirstname.text = fullName.dropLast(2).joinToString(separator = " ")
                }
                else -> {
                    castSurname.text = fullName.last()
                    castFirstname.text = fullName.dropLast(1).joinToString(separator = " ")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setDataCast(data: List<Cast>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
        if(list.isNullOrEmpty()){
            castTextInfo.visibility = View.INVISIBLE
        }
    }
}