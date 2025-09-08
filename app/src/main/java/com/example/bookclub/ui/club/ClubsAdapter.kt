package com.example.bookclub.ui.club

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bookclub.R
import com.example.bookclub.data.model.ClubStatus
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

class ClubsAdapter(
    val onPrimaryClick: (UiClub) -> Unit,
    val onLeaveClick: (UiClub) -> Unit,
    val onCardClick: (UiClub) -> Unit
) : ListAdapter<UiClub, ClubsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<UiClub>() {
        override fun areItemsTheSame(a: UiClub, b: UiClub) = a.club.id == b.club.id
        override fun areContentsTheSame(a: UiClub, b: UiClub) = a == b
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.imgCover)
        private val title: TextView = view.findViewById(R.id.txtTitle)
        private val author: TextView = view.findViewById(R.id.txtAuthor)
        private val start: TextView = view.findViewById(R.id.txtStart)
        private val countdown: TextView = view.findViewById(R.id.txtCountdown)
        private val status: TextView = view.findViewById(R.id.txtStatus)
        private val btnJoinOpen: Button = view.findViewById(R.id.btnJoinOpen)
        private val btnLeave: Button = view.findViewById(R.id.btnLeave)

        fun bindStatic(ui: UiClub) {
            val item = ui.club
            title.text = item.title
            author.text = item.author
            start.text = "Starts: ${item.startAt.toPrettyDate()}"

            val now = Instant.now()
            val effective = when {
                now.isBefore(item.startAt) -> ClubStatus.SCHEDULED
                now.isAfter(item.closeAt) -> ClubStatus.CLOSED
                else -> ClubStatus.LIVE
            }

            // Badge
            status.text = when (effective) {
                ClubStatus.LIVE -> "LIVE"
                ClubStatus.SCHEDULED -> "SCHEDULED"
                ClubStatus.CLOSED -> "CLOSED"
            }
            status.setBackgroundResource(R.drawable.badge_pill)
            status.background.setTint(
                ContextCompat.getColor(
                    status.context,
                    when (effective) {
                        ClubStatus.LIVE -> R.color.badgeLive
                        ClubStatus.SCHEDULED -> R.color.badgeScheduled
                        ClubStatus.CLOSED -> R.color.badgeClosed
                    }
                )
            )

            img.load(item.coverUrl) {
                placeholder(R.drawable.ic_book_placeholder)
                error(R.drawable.ic_book_placeholder)
                crossfade(true)
            }

            updateCountdown(item)

            // Logica butoanelor
            btnJoinOpen.text = if (ui.isMember)
                status.context.getString(R.string.club_open)
            else
                status.context.getString(R.string.club_join)

            btnJoinOpen.isEnabled = when (effective) {
                ClubStatus.SCHEDULED, ClubStatus.LIVE -> true
                ClubStatus.CLOSED -> ui.isMember
            }

            btnJoinOpen.setOnClickListener { onPrimaryClick(ui) }

            btnLeave.visibility = if (ui.isMember) View.VISIBLE else View.GONE
            btnLeave.setOnClickListener { onLeaveClick(ui) }

            itemView.setOnClickListener { onCardClick(ui) }
        }

        fun updateCountdown(item: com.example.bookclub.data.db.BookClubEntity) {
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

                else -> countdown.visibility = View.GONE
            }
        }
    }

    private val PAYLOAD_TICK = Any()
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            if (itemCount > 0) notifyItemRangeChanged(0, itemCount, PAYLOAD_TICK)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_club, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bindStatic(getItem(position))

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.any { it === PAYLOAD_TICK }) {
            holder.updateCountdown(getItem(position).club)
        } else super.onBindViewHolder(holder, position, payloads)
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

/* helpers */
private val prettyFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy, HH:mm")
    .withZone(ZoneId.systemDefault())

private fun Instant.toPrettyDate(): String = prettyFormatter.format(this)

private fun Duration.asHms(): String {
    val total = max(0, seconds)
    val d = total / 86_400
    val h = (total % 86_400) / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (d > 0) "%dd %02d:%02d:%02d".format(d, h, m, s)
    else "%02d:%02d:%02d".format(h, m, s)
}
