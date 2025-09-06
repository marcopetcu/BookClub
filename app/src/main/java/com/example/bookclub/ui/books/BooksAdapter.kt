package com.example.bookclub.ui.books

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bookclub.R
import com.example.bookclub.data.model.BookSearchItem

class BooksAdapter(
    private val onClick: (BookSearchItem) -> Unit
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {

    private val items = mutableListOf<BookSearchItem>()

    fun submitList(list: List<BookSearchItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_title)
        private val author: TextView = itemView.findViewById(R.id.tv_author)
        private val cover: ImageView = itemView.findViewById(R.id.img_cover)

        fun bind(book: BookSearchItem) {
            title.text = book.title
            author.text = book.author
            cover.load(book.coverUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
            }
            itemView.setOnClickListener { onClick(book) }
        }
    }
}
