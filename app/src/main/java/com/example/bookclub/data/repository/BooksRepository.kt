package com.example.bookclub.data.repository

import com.example.bookclub.data.db.dao.FollowBookDao
import com.example.bookclub.data.db.FollowBookEntity
import com.example.bookclub.data.model.BookSearchItem
import com.example.bookclub.data.model.BookWorkDetails
import com.example.bookclub.data.network.NetworkModule
import com.example.bookclub.data.network.toBookSearchItem
import com.example.bookclub.data.network.toBookWorkDetails
import com.example.bookclub.data.util.Result
import com.example.bookclub.data.util.runResult
import kotlinx.coroutines.flow.Flow

class BooksRepository(
    private val followBookDao: FollowBookDao
) {
    private val api = NetworkModule.openLibraryApi

    /** Căutare (HTTP #1) */
    suspend fun searchBooks(query: String): Result<List<BookSearchItem>> = runResult {
        val res = api.search(query)
        res.docs.map { it.toBookSearchItem() }
    }

    /** Detalii work (HTTP #2) */
    suspend fun getWorkDetails(workId: String): Result<BookWorkDetails> = runResult {
        api.workDetails(workId).toBookWorkDetails(workId)
    }

    /** ---- FOLLOW / UNFOLLOW ---- */

    /** Verifică dacă utilizatorul urmărește deja cartea */
    suspend fun isFollowing(userId: Long, workId: String): Boolean =
        followBookDao.hasFollow(userId, workId)

    /** Urmărește cartea (Upsert – nu aruncă dacă există) */
    suspend fun followBook(userId: Long, workId: String) {
        followBookDao.follow(FollowBookEntity(userId, workId))
    }

    /** Anulare follow */
    suspend fun unfollowBook(userId: Long, workId: String) {
        followBookDao.unfollow(userId, workId)
    }

    /** Toate workId-urile urmărite de utilizator (pentru a le afisa pe Profile/Home) */
    fun followedWorkIds(userId: Long): Flow<List<String>> =
        followBookDao.getFollowedWorkIds(userId)
}
