package com.example.bookclub.data.db.dao

import androidx.room.*
import com.example.bookclub.data.db.InboxEntity
import kotlinx.coroutines.flow.Flow

// DAO Room pentru Inbox: metode CRUD si query-uri specifice
@Dao
interface InboxDao {
    @Insert
    suspend fun insert(n: InboxEntity): Long

    @Query("SELECT * FROM inbox WHERE userId = :userId ORDER BY createdAt DESC")
    fun getForUser(userId: Long): Flow<List<InboxEntity>>

    @Query("UPDATE inbox SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE inbox SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: Long)
}
