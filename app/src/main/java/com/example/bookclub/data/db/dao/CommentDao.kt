package com.example.bookclub.data.db.dao

import androidx.room.*
import com.example.bookclub.data.db.CommentEntity
import kotlinx.coroutines.flow.Flow

// DAO Room pentru Comment: metode CRUD si query-uri specifice
@Dao
interface CommentDao {
    @Insert
    suspend fun insert(comment: CommentEntity): Long

    @Query("SELECT * FROM comment WHERE clubId = :clubId AND parentId IS NULL ORDER BY createdAt DESC")
    fun getTopLevel(clubId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comment WHERE parentId = :parentId ORDER BY createdAt ASC")
    fun getReplies(parentId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comment WHERE id = :id")
    suspend fun getById(id: Long): com.example.bookclub.data.db.CommentEntity?
}
