package com.example.bookclub.data.db.dao

import androidx.room.*
import com.example.bookclub.data.db.VoteEntity

@Dao
interface VoteDao {
    @Upsert
    suspend fun upsert(vote: VoteEntity)

    @Query("SELECT COALESCE(SUM(value), 0) FROM vote WHERE commentId = :commentId")
    fun getScore(commentId: Long): kotlinx.coroutines.flow.Flow<Int>
}
