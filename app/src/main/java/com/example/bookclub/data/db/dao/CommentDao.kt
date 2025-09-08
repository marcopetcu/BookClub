package com.example.bookclub.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookclub.data.db.CommentEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

// DAO Room pentru Comment: metode CRUD si query-uri specifice
@Dao
interface CommentDao {

    @Insert
    suspend fun insert(comment: CommentEntity): Long

    // Comentarii top-level + autor (nickname/email)
    @Query("""
        SELECT 
            c.id            AS id,
            c.clubId        AS clubId,
            c.userId        AS userId,
            c.content       AS content,
            c.createdAt     AS createdAt,
            u.nickname      AS authorNickname,
            u.email         AS authorEmail
        FROM comment c
        JOIN user u ON u.id = c.userId
        WHERE c.clubId = :clubId AND c.parentId IS NULL
        ORDER BY c.createdAt DESC
    """)
    fun getTopLevelWithAuthor(clubId: Long): Flow<List<CommentWithAuthor>>

    @Query("SELECT * FROM comment WHERE parentId = :parentId ORDER BY createdAt ASC")
    fun getReplies(parentId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comment WHERE id = :id")
    suspend fun getById(id: Long): CommentEntity?
}

/** DTO pentru JOIN (Room mapează după numele coloanelor aliate în query). */
data class CommentWithAuthor(
    val id: Long,
    val clubId: Long,
    val userId: Long,
    val content: String,
    val createdAt: Instant,
    val authorNickname: String?,
    val authorEmail: String
)
