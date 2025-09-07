package com.example.bookclub.ui.club

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.VH>() {

    private val items = mutableListOf<String>()

    inner class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = items[position]
    }

    override fun getItemCount() = items.size

    fun addComment(comment: String) {
        items.add(0, comment) // comentariul cel mai nou apare primul
        notifyItemInserted(0)
    }
}
