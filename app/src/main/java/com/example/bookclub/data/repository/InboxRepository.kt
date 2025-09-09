package com.example.bookclub.data.repository

import com.example.bookclub.data.db.InboxEntity
import com.example.bookclub.data.db.dao.InboxDao
import kotlinx.coroutines.flow.Flow

class InboxRepository(private val dao: InboxDao) {
    fun stream(userId: Long): Flow<List<InboxEntity>> = dao.streamForUser(userId)
    fun unreadCount(userId: Long): Flow<Int> = dao.unreadCount(userId)
    suspend fun markRead(id: Long) = dao.markRead(id)
    suspend fun markAllRead(userId: Long) = dao.markAllRead(userId)
}
