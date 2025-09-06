package com.example.bookclub.data.db.dao

import androidx.room.*
import com.example.bookclub.data.db.FollowUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUserDao {
    @Upsert
    suspend fun follow(e: FollowUserEntity)

    @Query("DELETE FROM follow_user WHERE followerId = :follower AND following = :following")
    suspend fun unfollow(follower: Long, following: Long)

    @Query("SELECT following FROM follow_user WHERE followerId = :userId")
    fun getFollowingIds(userId: Long): Flow<List<Long>>

    @Query("SELECT followerId FROM follow_user WHERE following = :userId")
    fun getFollowerIds(userId: Long): Flow<List<Long>>
}
