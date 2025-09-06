package com.example.bookclub.data.repository

import com.example.bookclub.data.db.InboxEntity
import com.example.bookclub.data.db.dao.InboxDao
import kotlinx.coroutines.flow.Flow

class InboxRepository(private val inboxDao: InboxDao) {
    fun inboxForUser(userId: Long): Flow<List<InboxEntity>> = inboxDao.getForUser(userId)
    suspend fun markRead(id: Long) = inboxDao.markRead(id)
    suspend fun markAllRead(userId: Long) = inboxDao.markAllRead(userId)
}
