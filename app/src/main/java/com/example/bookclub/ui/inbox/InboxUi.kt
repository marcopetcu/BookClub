package com.example.bookclub.ui.inbox

import java.time.Instant

data class InboxUi(
    val id: Long,
    val clubId: Long,          // non-null
    val title: String,         // non-null
    val coverUrl: String?,     // poate lipsi
    val startAt: Instant?,     // poate lipsi
    val createdAt: Instant,
    val isRead: Boolean
)
