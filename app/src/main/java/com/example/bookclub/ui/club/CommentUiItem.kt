package com.example.bookclub.ui.club

import com.example.bookclub.data.model.ClubComment

sealed class CommentUiItem {
    data class Parent(val data: ClubComment, val expanded: Boolean, val repliesCount: Int) : CommentUiItem()
    data class Reply(val parentId: Long, val data: ClubComment) : CommentUiItem()
}