package com.example.bookclub.data.model

import java.time.Instant

// Modele domeniu folosite in app (ClubComment.kt)
data class ClubComment(
    val id: Long,
    val clubId: Long,
    val userId: Long,
    val content: String,
    val createdAt: Instant
)
