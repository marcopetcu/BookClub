package com.example.bookclub.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookclub.data.db.InboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxDao {

    @Insert
    suspend fun insert(item: InboxEntity)

    @Query("SELECT * FROM inbox WHERE userId = :userId ORDER BY createdAt DESC")
    fun streamForUser(userId: Long): Flow<List<InboxEntity>>

    @Query("SELECT COUNT(*) FROM inbox WHERE userId = :userId AND isRead = 0")
    fun unreadCount(userId: Long): Flow<Int>

    @Query("UPDATE inbox SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE inbox SET isRead = 1 WHERE userId = :userId AND isRead = 0")
    suspend fun markAllRead(userId: Long)
}
