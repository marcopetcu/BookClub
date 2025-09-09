// file: app/src/main/java/com/example/bookclub/ui/club/ClubDetailViewModel.kt
package com.example.bookclub.ui.club

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.db.BookClubEntity
import com.example.bookclub.data.model.ClubComment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant

class ClubDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val commentsRepo = ServiceLocator.commentsRepository(app)
    private val clubsRepo    = ServiceLocator.clubsRepository(app)

    // ---- lista PĂRINȚI (top-level) ----
    private val _comments = MutableStateFlow<List<ClubComment>>(emptyList())
    val comments: StateFlow<List<ClubComment>> = _comments

    /** Pornește observarea comentariilor top-level pentru un club. */
    fun observeComments(clubId: Long) = viewModelScope.launch {
        commentsRepo.getComments(clubId).collectLatest { list ->
            _comments.value = list
        }
    }

    /** Postează un comentariu (sau reply dacă parentId != null). */
    fun postComment(
        clubId: Long,
        userId: Long,
        content: String,
        parentId: Long? = null
    ) = viewModelScope.launch {
        commentsRepo.insertComment(
            clubId = clubId,
            userId = userId,
            content = content,
            parentId = parentId
        )
        // top-level se actualizează automat din Flow
    }

    /** Ia o singură dată replicile pentru un comentariu părinte (lazy, la expand). */
    suspend fun fetchReplies(parentId: Long): List<ClubComment> {
        // dacă ai o metodă dedicată în repo:
        return commentsRepo.getRepliesOnce(parentId)
    }

    /** Numărul de replici pentru un părinte (pentru eticheta "Show/Hide replies (N)"). */
    suspend fun countReplies(parentId: Long): Int = commentsRepo.countReplies(parentId)

    // ---- Live / Not live -> permite comentarii? ----
    private val _isCommentingAllowed = MutableStateFlow(true)
    val isCommentingAllowed: StateFlow<Boolean> = _isCommentingAllowed

    /**
     * Observă starea clubului și setează dacă se pot face comentarii.
     * Ca sursă folosim `listAll()` din repo și derivăm pentru clubId cerut.
     * (Nu trebuie să modifici DAO/Repo.)
     */
    fun observeLiveState(clubId: Long) = viewModelScope.launch {
        clubsRepo
            .listAll() // Flow<List<BookClubEntity>>
            .map { list ->
                val club: BookClubEntity? = list.firstOrNull { it.id == clubId }
                club?.let { isLiveNow(it) } ?: false
            }
            .collect { allowed -> _isCommentingAllowed.value = allowed }
    }

    private fun isLiveNow(c: BookClubEntity): Boolean {
        val now = Instant.now()
        // dacă ai deja un status în entitate, îl poți folosi în loc:
        // return c.status == ClubStatus.LIVE
        return now.isAfter(c.startAt) && now.isBefore(c.closeAt)
    }
}
