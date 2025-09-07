package com.example.bookclub.ui.club

import java.time.Instant

data class ClubComment(
    val id: Long,
    val clubId: Long,
    val authorName: String,
    val content: String,
    val createdAt: Instant
)
