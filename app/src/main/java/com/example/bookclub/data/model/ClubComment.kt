package com.example.bookclub.data.model

import java.time.Instant

data class ClubComment(
    val id: Long,
    val clubId: Long,
    val userId: Long,
    val authorName: String,
    val content: String,
    val createdAt: Instant
)
