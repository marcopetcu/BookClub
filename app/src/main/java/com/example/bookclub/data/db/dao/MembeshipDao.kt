package com.example.bookclub.data.db.dao

import androidx.room.*
import com.example.bookclub.data.db.MembershipEntity
import com.example.bookclub.data.db.BookClubEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MembershipDao {
    @Upsert
    suspend fun upsert(m: MembershipEntity)

    @Query("DELETE FROM membership WHERE userId = :userId AND clubId = :clubId")
    suspend fun delete(userId: Long, clubId: Long)

    @Query("""
        SELECT bc.* FROM bookclub bc
        INNER JOIN membership m ON m.clubId = bc.id
        WHERE m.userId = :userId
        ORDER BY bc.startAt ASC
    """)
    fun getClubsForUser(userId: Long): Flow<List<BookClubEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM membership WHERE userId = :userId AND clubId = :clubId)")
    suspend fun isMember(userId: Long, clubId: Long): Boolean
}
