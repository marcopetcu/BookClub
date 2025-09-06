package com.example.bookclub.data.model

data class BookWorkDetails(
    val workId: String,
    val title: String,
    val description: String?,
    val subjects: List<String>,
    val coverUrl: String?
)
