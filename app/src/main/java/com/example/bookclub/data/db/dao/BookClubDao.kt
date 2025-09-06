package com.example.bookclub.data.db.dao

import androidx.room.*
import com.example.bookclub.data.db.BookClubEntity
import com.example.bookclub.data.model.ClubStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookClubDao {
    @Upsert
    suspend fun upsert(club: BookClubEntity): Long

    @Query("SELECT * FROM bookclub ORDER BY startAt ASC")
    fun getAllOrderByStart(): Flow<List<BookClubEntity>>

    @Query("SELECT * FROM bookclub WHERE title LIKE :q OR author LIKE :q ORDER BY startAt ASC")
    fun search(q: String): Flow<List<BookClubEntity>>

    @Query("SELECT * FROM bookclub WHERE workId = :workId ORDER BY startAt DESC")
    fun getForWorkId(workId: String): Flow<List<BookClubEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookclub WHERE workId = :workId AND status IN (:active1, :active2))")
    suspend fun existsActiveForWork(workId: String, active1: ClubStatus = ClubStatus.SCHEDULED, active2: ClubStatus = ClubStatus.LIVE): Boolean

    @Query("SELECT * FROM bookclub WHERE id = :id")
    suspend fun getById(id: Long): BookClubEntity?

    @Query("""
    SELECT bc.* FROM bookclub bc
    JOIN follow_book fb ON fb.workId = bc.workId
    WHERE fb.userId = :userId AND bc.status IN (:s1, :s2)
    ORDER BY bc.startAt ASC
    """)
    fun listForFollowedBooks(
        userId: Long,
        s1: com.example.bookclub.data.model.ClubStatus = com.example.bookclub.data.model.ClubStatus.SCHEDULED,
        s2: com.example.bookclub.data.model.ClubStatus = com.example.bookclub.data.model.ClubStatus.LIVE
    ): kotlinx.coroutines.flow.Flow<List<com.example.bookclub.data.db.BookClubEntity>>

}
