package com.example.bookclub.ui.club

import com.example.bookclub.data.db.BookClubEntity

data class UiClub(
    val club: BookClubEntity,
    val isMember: Boolean
)
