package com.example.bookclub.ui.club

import com.example.bookclub.data.model.ClubComment

sealed class ThreadItem {
    data class Parent(
        val comment: ClubComment,
        val isExpanded: Boolean,
        val repliesCount: Int? // null dacă nu știm încă
    ) : ThreadItem()

    data class Reply(
        val comment: ClubComment
    ) : ThreadItem()
}
