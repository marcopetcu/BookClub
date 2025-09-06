package com.example.bookclub.ui.club

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.db.CommentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ClubDetailViewModel(app: Application) : AndroidViewModel(app) {
    private val commentsRepo = ServiceLocator.commentsRepository(app)
    private val db = ServiceLocator.db(app)

    private val clubId = MutableStateFlow<Long?>(null)
    fun setClub(id: Long) { clubId.value = id }

    val topLevelComments: Flow<List<CommentEntity>> =
        clubId.flatMapLatest { id ->
            id?.let { db.commentDao().getTopLevel(it) } ?: flowOf(emptyList())
        }

    fun replies(parentId: Long): Flow<List<CommentEntity>> =
        db.commentDao().getReplies(parentId)

    fun postComment(userId: Long, content: String, parentId: Long? = null) = viewModelScope.launch {
        val cid = clubId.value ?: return@launch
        commentsRepo.postComment(clubId = cid, userId = userId, content = content, parentId = parentId)
    }

    fun vote(commentId: Long, userId: Long, up: Boolean) = viewModelScope.launch {
        commentsRepo.vote(commentId, userId, if (up) 1 else -1)
    }
}
