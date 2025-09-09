package com.example.bookclub.ui.inbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import com.example.bookclub.data.db.InboxEntity
import org.json.JSONObject
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class InboxAdapter(
    private val onClick: (InboxEntity, Parsed) -> Unit
) : ListAdapter<InboxEntity, InboxAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<InboxEntity>() {
        override fun areItemsTheSame(a: InboxEntity, b: InboxEntity) = a.id == b.id
        override fun areContentsTheSame(a: InboxEntity, b: InboxEntity) = a == b
    }

    data class Parsed(
        val title: String,
        val subtitle: String,
        val clubId: Long? = null
    )

    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val parsed = parse(item)
        holder.bind(parsed)
        holder.itemView.setOnClickListener { onClick(item, parsed) }
    }

    private fun parse(item: InboxEntity): Parsed {
        val created = formatter.format(item.createdAt)
        return when (item.type) {
            "NEW_CLUB_FOR_FOLLOWED_BOOK" -> {
                val obj = JSONObject(item.payloadJson)
                val title = obj.optString("title", "Book Club")
                val clubId = obj.optLong("clubId", 0L).takeIf { it > 0 }
                Parsed(
                    title = title,
                    subtitle = holderText(item, R.string.inbox_new_club, created),
                    clubId = clubId
                )
            }
            else -> Parsed(
                title = "Notification",
                subtitle = created
            )
        }
    }

    private fun holderText(item: InboxEntity, resId: Int, created: String): String {
        // "New club scheduled • 09 Sep 2025, 19:00"
        return itemViewContext?.getString(resId)?.plus(" • ").orEmpty() + created
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.txtTitle)
        private val subtitle: TextView = view.findViewById(R.id.txtSubtitle)
        fun bind(p: Parsed) {
            title.text = p.title
            subtitle.text = p.subtitle
        }
    }

    // Trick să avem context în helper
    private var itemViewContext: android.content.Context? = null
    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        itemViewContext = holder.itemView.context
    }
}
