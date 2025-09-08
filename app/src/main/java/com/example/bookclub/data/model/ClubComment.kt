package com.example.bookclub.data.model

import java.time.Instant

data class ClubComment(
    val id: Long,
    val clubId: Long,
    val userId: Long,
    val content: String,
    val authorName: String,   // <- folosit în UI (nickname/email)
    val createdAt: Instant
)
