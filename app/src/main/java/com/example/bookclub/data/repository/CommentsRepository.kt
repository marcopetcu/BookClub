package com.example.bookclub.data.repository

import com.example.bookclub.data.db.CommentEntity
import com.example.bookclub.data.db.InboxEntity
import com.example.bookclub.data.db.VoteEntity
import com.example.bookclub.data.db.dao.CommentDao
import com.example.bookclub.data.db.dao.InboxDao
import com.example.bookclub.data.db.dao.VoteDao
import java.time.Instant

class CommentsRepository(
    private val commentDao: CommentDao,
    private val voteDao: VoteDao,
    private val inboxDao: InboxDao
) {
    suspend fun postComment(
        clubId: Long,
        userId: Long,
        content: String,
        parentId: Long? = null
    ): Long {
        val id = commentDao.insert(
            CommentEntity(
                clubId = clubId,
                userId = userId,
                content = content,
                createdAt = Instant.now(),
                parentId = parentId
            )
        )

        if (parentId != null) {
            val parent = commentDao.getById(parentId)
            if (parent != null && parent.userId != userId) {
                inboxDao.insert(
                    InboxEntity(
                        userId = parent.userId,
                        type = "COMMENT_REPLY",
                        payloadJson = """{"clubId":$clubId,"commentId":$id,"parentId":$parentId}""",
                        isRead = false,
                        createdAt = Instant.now()
                    )
                )
            }
        }
        return id
    }

    suspend fun vote(commentId: Long, userId: Long, value: Int) {
        val v = if (value >= 0) 1 else -1
        voteDao.upsert(
            VoteEntity(
                commentId = commentId,
                userId = userId,
                value = v,
                createdAt = Instant.now()
            )
        )
    }
}
