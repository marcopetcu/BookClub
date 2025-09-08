package com.example.bookclub.data.model

// Modele domeniu folosite in app (BookWorkDetails.kt)
data class BookWorkDetails(
    val workId: String,
    val title: String,
    val description: String?,
    val subjects: List<String>,
    val coverUrl: String?
)
