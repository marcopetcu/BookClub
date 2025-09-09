package com.example.bookclub.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bookclub.R
import com.example.bookclub.data.db.BookClubEntity

class FollowedAdapter :
    ListAdapter<BookClubEntity, FollowedAdapter.VH>(DIFF) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val cover: ImageView = view.findViewById(R.id.imgCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_followed, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val club = getItem(position)
        holder.title.text = club.title
        holder.cover.load(club.coverUrl) {
            placeholder(R.drawable.ic_book_placeholder)
            error(R.drawable.ic_book_placeholder)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BookClubEntity>() {
            override fun areItemsTheSame(a: BookClubEntity, b: BookClubEntity) = a.id == b.id
            override fun areContentsTheSame(a: BookClubEntity, b: BookClubEntity) = a == b
        }
    }
}
