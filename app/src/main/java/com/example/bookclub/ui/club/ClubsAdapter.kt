package com.example.bookclub.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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

            // status “efectiv” calculat din timp
            val now = Instant.now()
            val effectiveStatus = when {
                now.isBefore(item.startAt) -> ClubStatus.SCHEDULED
                now.isAfter(item.closeAt)  -> ClubStatus.CLOSED
                else                       -> ClubStatus.LIVE
            }

            // textul
            status.text = when (effectiveStatus) {
                ClubStatus.LIVE      -> "LIVE"
                ClubStatus.SCHEDULED -> "SCHEDULED"
                ClubStatus.CLOSED    -> "CLOSED"
            }

            // fundalul (shape comun + culoare diferită)
            status.setBackgroundResource(R.drawable.badge_pill)
            val color = when (effectiveStatus) {
                ClubStatus.LIVE      -> R.color.badgeLive
                ClubStatus.SCHEDULED -> R.color.badgeScheduled
                ClubStatus.CLOSED    -> R.color.badgeClosed
            }
            status.background.setTint(ContextCompat.getColor(status.context, color))
            status.setTextColor(ContextCompat.getColor(status.context, R.color.badgeTextOn))

            // coperta
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
            .inflate(R.layout.item_club, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}

private val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy, HH:mm")
    .withZone(ZoneId.systemDefault())

private fun Instant.toPrettyDate(): String = formatter.format(this)
