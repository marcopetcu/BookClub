package com.example.bookclub.ui.home

import android.os.Handler
import android.os.Looper
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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

class ClubsAdapter(
    private val onClick: (BookClubEntity) -> Unit
) : ListAdapter<BookClubEntity, ClubsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<BookClubEntity>() {
        override fun areItemsTheSame(a: BookClubEntity, b: BookClubEntity) = a.id == b.id
        override fun areContentsTheSame(a: BookClubEntity, b: BookClubEntity) = a == b
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgCover)
        val title: TextView = view.findViewById(R.id.txtTitle)
        val author: TextView = view.findViewById(R.id.txtAuthor)
        val start: TextView = view.findViewById(R.id.txtStart)
        val status: TextView = view.findViewById(R.id.txtStatus)
        val countdown: TextView = view.findViewById(R.id.txtCountdown)

        fun bindStatic(item: BookClubEntity) {
            title.text = item.title
            author.text = item.author
            start.text  = "Starts: ${item.startAt.toPrettyDate()}"

            val now = Instant.now()
            val effective = when {
                now.isBefore(item.startAt) -> ClubStatus.SCHEDULED
                now.isAfter(item.closeAt)  -> ClubStatus.CLOSED
                else                       -> ClubStatus.LIVE
            }

            val (label, colorRes) = when (effective) {
                ClubStatus.LIVE      -> "LIVE"       to R.color.badgeLive
                ClubStatus.SCHEDULED -> "SCHEDULED"  to R.color.badgeScheduled
                ClubStatus.CLOSED    -> "CLOSED"     to R.color.badgeClosed
            }
            status.text = label
            status.setBackgroundResource(R.drawable.badge_pill)
            status.background.setTint(ContextCompat.getColor(status.context, colorRes))

            img.load(item.coverUrl) {
                placeholder(R.drawable.ic_book_placeholder)
                error(R.drawable.ic_book_placeholder)
                crossfade(true)
            }

            // prima actualizare a countdown-ului
            updateCountdown(item)

            itemView.setOnClickListener { onClick(item) }
        }

        fun updateCountdown(item: BookClubEntity) {
            val now = Instant.now()
            when {
                now.isBefore(item.startAt) -> {
                    val left = Duration.between(now, item.startAt)
                    countdown.visibility = View.VISIBLE
                    countdown.text = "Starts in ${left.asHms()}"
                }
                now.isBefore(item.closeAt) -> {
                    val left = Duration.between(now, item.closeAt)
                    countdown.visibility = View.VISIBLE
                    countdown.text = "Live: ${left.asHms()} left"
                }
                else -> {
                    countdown.visibility = View.GONE
                }
            }
        }
    }

    // payload ușor pentru „tick” (evită rebind complet)
    private val PAYLOAD_TICK = Any()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_club, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bindStatic(getItem(position))
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.any { it === PAYLOAD_TICK }) {
            holder.updateCountdown(getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    // ticker global al adapterului (1s)
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            if (itemCount > 0) {
                notifyItemRangeChanged(0, itemCount, PAYLOAD_TICK)
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        handler.post(ticker)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        handler.removeCallbacksAndMessages(null)
    }
}

/* ------- helpers ------- */

private val prettyFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy, HH:mm")
    .withZone(ZoneId.systemDefault())

private fun Instant.toPrettyDate(): String = prettyFormatter.format(this)

/** Format „HH:mm:ss” (sau „dd:HH:mm:ss” dacă e > 24h). */
private fun Duration.asHms(): String {
    val totalSec = max(0, this.seconds)
    val days = totalSec / 86_400
    val hours = (totalSec % 86_400) / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (days > 0)
        String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
    else
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
