package com.example.bookclub.ui.inbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bookclub.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class InboxAdapter(
    private val onClick: (InboxUi) -> Unit
) : ListAdapter<InboxUi, InboxAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<InboxUi>() {
        override fun areItemsTheSame(a: InboxUi, b: InboxUi) = a.id == b.id
        override fun areContentsTheSame(a: InboxUi, b: InboxUi) = a == b
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.imgCover)
        private val title: TextView = view.findViewById(R.id.txtTitle)
        private val whenTxt: TextView = view.findViewById(R.id.txtWhen)
        private val dot: View = view.findViewById(R.id.unreadDot)

        fun bind(item: InboxUi) {
            val ctx = itemView.context

            title.text = item.title.ifBlank { ctx.getString(R.string.unknown_title) }
            whenTxt.text = item.startAt?.let { it.pretty(ctx.getString(R.string.starts_at_fmt)) } ?: ""

            dot.isVisible = !item.isRead

            img.load(item.coverUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_book_placeholder)
                error(R.drawable.ic_book_placeholder)
            }

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}

/* ----- helpers ----- */

private val inboxFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())

private fun Instant.pretty(prefixFmt: String): String =
    prefixFmt.format(inboxFormatter.format(this))
