package com.example.bookclub.data

import android.content.Context
import com.example.bookclub.data.db.AppDatabase
import com.example.bookclub.data.repository.*
import com.example.bookclub.data.session.SessionManager

object ServiceLocator {
    fun db(context: Context) = AppDatabase.get(context)

    fun booksRepository(context: Context) =
        BooksRepository(db(context).followBookDao())

    fun clubsRepository(context: Context) =
        ClubsRepository(
            db(context).bookClubDao(),
            db(context).membershipDao(),
            db(context).followBookDao(),
            db(context).inboxDao()
        )

    fun commentsRepository(context: Context) =
        CommentsRepository(
            db(context).commentDao(),
            db(context).voteDao(),
            db(context).inboxDao()
        )

    fun inboxRepository(context: Context) =
        InboxRepository(db(context).inboxDao())

    fun authRepository(context: Context) =
        AuthRepository(db(context).userDao())

    fun sessionManager(context: Context) =
        SessionManager(context)
}
