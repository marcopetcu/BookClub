package com.example.bookclub.ui.inbox

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.db.InboxEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InboxViewModel(app: Application) : AndroidViewModel(app) {
    private val session = ServiceLocator.sessionManager(app)
    private val repo = ServiceLocator.inboxRepository(app)
    private val userId get() = session.currentUserId ?: 1L

    val items: StateFlow<List<InboxEntity>> =
        repo.stream(userId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unread: StateFlow<Int> =
        repo.unreadCount(userId).stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun markRead(id: Long) = viewModelScope.launch { repo.markRead(id) }
    fun markAll() = viewModelScope.launch { repo.markAllRead(userId) }
}
