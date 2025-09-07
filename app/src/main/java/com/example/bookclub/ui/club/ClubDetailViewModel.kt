package com.example.bookclub.ui.club

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.model.ClubComment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ClubDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val commentsRepo = ServiceLocator.commentsRepository(app)

    private val _comments = MutableStateFlow<List<ClubComment>>(emptyList())
    val comments: StateFlow<List<ClubComment>> = _comments

    /** Observă comentariile pentru un club. */
    fun observeComments(clubId: Long) = viewModelScope.launch {
        commentsRepo.getComments(clubId).collectLatest { list ->
            _comments.value = list
        }
    }

    /** Postează un comentariu nou. */
    fun postComment(clubId: Long, userId: Long, content: String) = viewModelScope.launch {
        commentsRepo.insertComment(clubId, userId, content)
        // lista se actualizează singură pentru că e Flow
    }
}
