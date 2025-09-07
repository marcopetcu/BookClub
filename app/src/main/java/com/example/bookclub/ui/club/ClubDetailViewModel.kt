package com.example.bookclub.ui.club

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class ClubDetailViewModel(app: Application) : AndroidViewModel(app) {

    // listă locală pentru demo; înlocuiești ușor cu repo când ai backend
    private val _comments = MutableStateFlow<List<ClubComment>>(emptyList())
    val comments: StateFlow<List<ClubComment>> = _comments

    private val idGen = AtomicLong(1L)

    // TODO: ia numele real din SessionManager sau profilul utilizatorului
    private val currentUserName: String = "Me"

    fun loadComments(clubId: Long) = viewModelScope.launch {
        // mock: nimic de încărcat deocamdată; la repo ai apela getComments(clubId)
    }

    fun postComment(clubId: Long, content: String) = viewModelScope.launch {
        val newItem = ClubComment(
            id = idGen.getAndIncrement(),
            clubId = clubId,
            authorName = currentUserName,
            content = content,
            createdAt = Instant.now()
        )
        _comments.value = _comments.value + newItem
    }
}
