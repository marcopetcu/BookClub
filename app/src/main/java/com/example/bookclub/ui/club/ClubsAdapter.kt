package com.example.bookclub.ui.home

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
import com.example.bookclub.data.model.ClubStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClubsAdapter(
    private val onClick: (BookClubEntity) -> Unit
) : ListAdapter<BookClubEntity, ClubsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<BookClubEntity>() {
        override fun areItemsTheSame(a: BookClubEntity, b: BookClubEntity) = a.id == b.id
        override fun areContentsTheSame(a: BookClubEntity, b: BookClubEntity) = a == b
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.imgCover)
        private val title: TextView = view.findViewById(R.id.txtTitle)
        private val author: TextView = view.findViewById(R.id.txtAuthor)
        private val start: TextView = view.findViewById(R.id.txtStart)
        private val status: TextView = view.findViewById(R.id.txtStatus)

        fun bind(item: BookClubEntity) {
            title.text = item.title
            author.text = item.author
            start.text = "Starts: ${item.startAt.toPrettyDate()}"
            status.text = item.status.name

            // flag status
            val bg = when (item.status) {
                ClubStatus.LIVE -> R.drawable.badge_live // opțional; altfel comentează
                ClubStatus.SCHEDULED -> R.drawable.badge_scheduled
                ClubStatus.CLOSED -> R.drawable.badge_closed
            }
            status.setBackgroundResource(bg)

            img.load(item.coverUrl) {
                placeholder(R.drawable.ic_book_placeholder)
                error(R.drawable.ic_book_placeholder)
                crossfade(true)
            }

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_club, parent, false)   // <- item_book
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}

private val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy, HH:mm")
    .withZone(ZoneId.systemDefault())

private fun Instant.toPrettyDate(): String = formatter.format(this)
