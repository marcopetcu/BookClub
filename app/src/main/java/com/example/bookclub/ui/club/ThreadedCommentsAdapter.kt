// file: app/src/main/java/com/example/bookclub/ui/club/ThreadedCommentsAdapter.kt
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

class ThreadedCommentsAdapter(
    private val onReplyClick: (ClubComment) -> Unit,
    private val onToggleParent: (parentId: Long, currentlyExpanded: Boolean) -> Unit,
    canReply: Boolean = true
) : ListAdapter<ThreadItem, RecyclerView.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<ThreadItem>() {
        override fun areItemsTheSame(a: ThreadItem, b: ThreadItem): Boolean =
            when {
                a is ThreadItem.Parent && b is ThreadItem.Parent -> a.comment.id == b.comment.id
                a is ThreadItem.Reply  && b is ThreadItem.Reply  -> a.comment.id == b.comment.id
                else -> false
            }
        override fun areContentsTheSame(a: ThreadItem, b: ThreadItem): Boolean = a == b
    }

    private companion object {
        const val VT_PARENT = 1
        const val VT_REPLY  = 2
    }

    // ✅ flag global pentru a arăta/ascunde butonul Reply
    private var canReplyInternal: Boolean = canReply
    fun setCanReply(value: Boolean) {
        if (canReplyInternal != value) {
            canReplyInternal = value
            // re-leagă vizibilitatea butonului Reply
            notifyItemRangeChanged(0, itemCount, /* payload */ "reply-visibility")
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ThreadItem.Parent -> VT_PARENT
            is ThreadItem.Reply  -> VT_REPLY
        }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (vt) {
            VT_PARENT -> ParentVH(inf.inflate(R.layout.item_comment_parent, parent, false))
            VT_REPLY  -> ReplyVH (inf.inflate(R.layout.item_reply,          parent, false))
            else      -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        when (val item = getItem(pos)) {
            is ThreadItem.Parent -> (h as ParentVH).bind(item, onReplyClick, onToggleParent, canReplyInternal)
            is ThreadItem.Reply  -> (h as ReplyVH ).bind(item.comment)
        }
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int, payloads: MutableList<Any>) {
        if (payloads.contains("reply-visibility") && h is ParentVH) {
            // actualizează DOAR vizibilitatea butonului Reply, fără rebind complet
            h.updateReplyVisibility(canReplyInternal)
            return
        }
        super.onBindViewHolder(h, pos, payloads)
    }

    inner class ParentVH(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAuthor: TextView = view.findViewById(R.id.txtAuthor)
        private val txtTime:   TextView = view.findViewById(R.id.txtTime)
        private val txtContent:TextView = view.findViewById(R.id.txtContent)
        private val btnReply:  TextView = view.findViewById(R.id.btnReply)
        private val btnToggle: TextView = view.findViewById(R.id.btnToggleReplies)

        private lateinit var boundComment: ClubComment
        private var isExpandedBound: Boolean = false

        fun bind(
            item: ThreadItem.Parent,
            onReplyClick: (ClubComment) -> Unit,
            onToggleParent: (Long, Boolean) -> Unit,
            canReply: Boolean
        ) {
            val c = item.comment
            boundComment = c
            isExpandedBound = item.isExpanded

            txtAuthor.text  = c.authorName?.ifBlank { "Anonymous" } ?: "Anonymous"
            txtTime.text    = formatter.format(c.createdAt.atZone(ZoneId.systemDefault()))
            txtContent.text = c.content

            // Toggle „Show/Hide replies”
            val count = item.repliesCount
            if (count != null && count > 0) {
                btnToggle.visibility = View.VISIBLE
                btnToggle.text = if (item.isExpanded)
                    itemView.context.getString(R.string.hide_replies_fmt, count)
                else
                    itemView.context.getString(R.string.show_replies_fmt, count)
            } else {
                btnToggle.visibility = View.GONE
            }
            btnToggle.setOnClickListener { onToggleParent(c.id, item.isExpanded) }

            // Reply – controlat de canReply
            updateReplyVisibility(canReply)
            btnReply.setOnClickListener {
                if ((btnReply.visibility == View.VISIBLE)) onReplyClick(c)
            }
        }

        fun updateReplyVisibility(canReply: Boolean) {
            btnReply.visibility = if (canReply) View.VISIBLE else View.GONE
        }
    }

    inner class ReplyVH(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAuthor: TextView = view.findViewById(R.id.txtAuthor)
        private val txtTime:   TextView = view.findViewById(R.id.txtTime)
        private val txtContent:TextView = view.findViewById(R.id.txtContent)

        fun bind(c: ClubComment) {
            txtAuthor.text  = c.authorName?.ifBlank { "Anonymous" } ?: "Anonymous"
            txtTime.text    = formatter.format(c.createdAt.atZone(ZoneId.systemDefault()))
            txtContent.text = c.content
        }
    }
}

private val formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
