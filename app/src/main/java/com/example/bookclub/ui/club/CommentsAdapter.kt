@file:JvmName("CommentsAdapterKt")

package com.example.bookclub.ui.club

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CommentsAdapter :
    ListAdapter<ClubComment, CommentsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ClubComment>() {
        override fun areItemsTheSame(a: ClubComment, b: ClubComment) = a.id == b.id
        override fun areContentsTheSame(a: ClubComment, b: ClubComment) = a == b
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAuthor: TextView = view.findViewById(R.id.txtAuthor)
        private val txtTime: TextView   = view.findViewById(R.id.txtTime)
        private val txtContent: TextView= view.findViewById(R.id.txtContent)

        fun bind(item: ClubComment) {
            txtAuthor.text = item.authorName
            txtTime.text   = item.createdAt.pretty()
            txtContent.text= item.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}

private val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy, HH:mm")
    .withZone(ZoneId.systemDefault())

private fun Instant.pretty(): String = formatter.format(this)
