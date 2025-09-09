// file: app/src/main/java/com/example/bookclub/ui/club/ClubDetailViewModel.kt
package com.example.bookclub.ui.club

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.model.ClubComment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClubDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val commentsRepo = ServiceLocator.commentsRepository(app)

    private val _comments = MutableStateFlow<List<ClubComment>>(emptyList())
    val comments: StateFlow<List<ClubComment>> = _comments

    fun observeComments(clubId: Long) = viewModelScope.launch {
        commentsRepo.getComments(clubId).collectLatest { list ->
            _comments.value = list
        }
    }

    // ------- nou: inserare "await" (util pt. a reîmprospăta imediat replies) -------
    suspend fun addCommentAwait(
        clubId: Long,
        userId: Long,
        content: String,
        parentId: Long? = null
    ): Long {
        return commentsRepo.insertComment(
            clubId = clubId,
            userId = userId,
            content = content,
            parentId = parentId
        )
    }
    // ------------------------------------------------------------------------------

    suspend fun fetchReplies(parentId: Long): List<ClubComment> {
        // Dacă ai funcție directă în repo, o poți folosi.
        return commentsRepo.getRepliesOnce(parentId)
    }

    suspend fun countReplies(parentId: Long): Int {
        return commentsRepo.countReplies(parentId)
    }
}
