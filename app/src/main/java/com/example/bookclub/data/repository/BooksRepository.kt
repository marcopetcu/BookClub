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

class BooksRepository(
    private val followBookDao: FollowBookDao
) {
    private val api = NetworkModule.openLibraryApi

    suspend fun searchBooks(query: String): Result<List<BookSearchItem>> = runResult {
        val res = api.search(query)
        res.docs.map { it.toBookSearchItem() }
    }

    suspend fun getWorkDetails(workId: String): Result<BookWorkDetails> = runResult {
        api.workDetails(workId).toBookWorkDetails(workId)
    }

    suspend fun followBook(userId: Long, workId: String) {
        followBookDao.follow(FollowBookEntity(userId, workId))
    }

    suspend fun unfollowBook(userId: Long, workId: String) {
        followBookDao.unfollow(userId, workId)
    }
}
