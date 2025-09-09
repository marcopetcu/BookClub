package com.example.bookclub.ui.club

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import com.example.bookclub.data.model.ClubComment
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Adapter pe două view types:
 *  - Parent (comentariu top-level) -> are buton Reply + Show/Hide replies
 *  - Reply (comentariu copil) -> fără buton Reply
 *
 * Folosește un model intern UiItem și un set de parentIds expandate.
 */
class CommentsAdapter(
    private val onReplyClick: (ClubComment) -> Unit
) : ListAdapter<CommentsAdapter.UiItem, RecyclerView.ViewHolder>(Diff) {

    /** Ce afișăm efectiv în listă */
    sealed class UiItem {
        data class Parent(
            val comment: ClubComment,
            val repliesCount: Int,
            val expanded: Boolean
        ) : UiItem()

        data class Reply(
            val comment: ClubComment
        ) : UiItem()
    }

    /** Starea structurală – sursa adevărului pentru construire listă vizibilă */
    private var parents: List<ClubComment> = emptyList()
    private var repliesByParent: Map<Long, List<ClubComment>> = emptyMap()
    private val expanded = mutableSetOf<Long>() // id-urile părinților expandați

    fun submitThread(
        parents: List<ClubComment>,
        repliesByParent: Map<Long, List<ClubComment>>
    ) {
        this.parents = parents
        this.repliesByParent = repliesByParent
        rebuild()
    }

    /** Reconstruiește lista vizibilă pe baza setului 'expanded' */
    private fun rebuild() {
        val items = buildList {
            for (p in parents) {
                val replies = repliesByParent[p.id].orEmpty()
                add(UiItem.Parent(p, replies.size, p.id in expanded))
                if (p.id in expanded && replies.isNotEmpty()) {
                    replies.forEach { r -> add(UiItem.Reply(r)) }
                }
            }
        }
        submitList(items)
    }

    /** Toggle expand/collapse pentru un părinte */
    private fun toggle(parentId: Long) {
        if (parentId in expanded) expanded.remove(parentId) else expanded.add(parentId)
        rebuild()
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is UiItem.Parent -> VIEW_PARENT
        is UiItem.Reply  -> VIEW_REPLY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == VIEW_PARENT) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            ParentVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_reply, parent, false)
            ReplyVH(v)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is UiItem.Parent -> (holder as ParentVH).bind(item)
            is UiItem.Reply  -> (holder as ReplyVH).bind(item)
        }
    }

    inner class ParentVH(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAuthor: TextView = view.findViewById(R.id.txtAuthor)
        private val txtDate: TextView   = view.findViewById(R.id.txtDate)
        private val txtContent: TextView= view.findViewById(R.id.txtContent)
        private val btnReply: TextView  = view.findViewById(R.id.btnReply)
        private val btnToggle: TextView = view.findViewById(R.id.btnToggleReplies)

        fun bind(ui: UiItem.Parent) {
            val c = ui.comment
            txtAuthor.text = c.authorName ?: "Anonymous"
            txtDate.text   = formatter.format(c.createdAt.atZone(ZoneId.systemDefault()))
            txtContent.text= c.content

            // Reply activ doar pe părinți
            btnReply.setOnClickListener { onReplyClick(c) }

            // Toggle show/hide replies
            if (ui.repliesCount > 0) {
                btnToggle.visibility = View.VISIBLE
                btnToggle.text = if (ui.expanded)
                    itemView.context.getString(R.string.hide_replies_fmt, ui.repliesCount)
                else
                    itemView.context.getString(R.string.show_replies_fmt, ui.repliesCount)

                btnToggle.setOnClickListener {
                    toggle(c.id)
                }
            } else {
                btnToggle.visibility = View.GONE
                btnToggle.setOnClickListener(null)
            }
        }
    }

    inner class ReplyVH(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAuthor: TextView = view.findViewById(R.id.txtAuthor)
        private val txtDate: TextView   = view.findViewById(R.id.txtDate)
        private val txtContent: TextView= view.findViewById(R.id.txtContent)

        fun bind(ui: UiItem.Reply) {
            val c = ui.comment
            txtAuthor.text = c.authorName ?: "Anonymous"
            txtDate.text   = formatter.format(c.createdAt.atZone(ZoneId.systemDefault()))
            txtContent.text= c.content
            // Fără buton Reply aici -> nimic de ascuns, nimic de arătat
        }
    }

    object Diff : DiffUtil.ItemCallback<UiItem>() {
        override fun areItemsTheSame(a: UiItem, b: UiItem): Boolean = when {
            a is UiItem.Parent && b is UiItem.Parent -> a.comment.id == b.comment.id
            a is UiItem.Reply  && b is UiItem.Reply  -> a.comment.id == b.comment.id
            else -> false
        }

        override fun areContentsTheSame(a: UiItem, b: UiItem): Boolean = a == b
    }

    companion object {
        private const val VIEW_PARENT = 0
        private const val VIEW_REPLY  = 1
    }
}

private val formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
