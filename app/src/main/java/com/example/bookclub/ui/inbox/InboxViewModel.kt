package com.example.bookclub.ui.inbox

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.db.InboxEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class InboxViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.inboxRepository(app)
    private val userId = MutableStateFlow<Long?>(null)
    fun setUser(id: Long) { userId.value = id }

    val inbox: Flow<List<InboxEntity>> =
        userId.flatMapLatest { id -> id?.let { repo.inboxForUser(it) } ?: flowOf(emptyList()) }

    fun markRead(id: Long) = viewModelScope.launch { repo.markRead(id) }
    fun markAllRead() = viewModelScope.launch {
        userId.value?.let { repo.markAllRead(it) }
    }
}
