package com.example.bookclub.data.model

// Modele domeniu folosite in app (BookSearchItem.kt)
data class BookSearchItem(
    val key: String,
    val title: String,
    val author: String?,
    val coverUrl: String?
)