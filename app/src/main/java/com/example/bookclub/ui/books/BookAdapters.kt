package com.example.bookclub.ui.books

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
import com.example.bookclub.data.model.BookSearchItem

// adapter pentru RecyclerVeiw
class BooksAdapter(
    private val onItemClick: (BookSearchItem) -> Unit
) : ListAdapter<BookSearchItem, BooksAdapter.BookViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<BookSearchItem>() {
        override fun areItemsTheSame(a: BookSearchItem, b: BookSearchItem) = a.key == b.key
        override fun areContentsTheSame(a: BookSearchItem, b: BookSearchItem) = a == b
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(v, onItemClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookViewHolder(
        itemView: View,
        private val onItemClick: (BookSearchItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val img: ImageView = itemView.findViewById(R.id.img_cover)
        private val title: TextView = itemView.findViewById(R.id.tv_title)
        private val author: TextView = itemView.findViewById(R.id.tv_author)

        // leaga datele din BookSearchItem de view-uri
        fun bind(item: BookSearchItem) {
            title.text = item.title
            author.text = item.author.orEmpty()
            img.load(item.coverUrl) {
                placeholder(R.drawable.ic_book_placeholder)
                error(R.drawable.ic_book_placeholder)
                crossfade(true)
            }
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
