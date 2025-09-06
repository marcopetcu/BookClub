package com.example.bookclub.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookclub.data.db.dao.*

@Database(
    entities = [
        UserEntity::class,
        BookClubEntity::class,
        MembershipEntity::class,
        CommentEntity::class,
        VoteEntity::class,
        FollowUserEntity::class,
        FollowBookEntity::class,
        InboxEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookClubDao(): BookClubDao
    abstract fun membershipDao(): MembershipDao
    abstract fun commentDao(): CommentDao
    abstract fun voteDao(): VoteDao
    abstract fun followUserDao(): FollowUserDao
    abstract fun followBookDao(): FollowBookDao
    abstract fun inboxDao(): InboxDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookclub.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
