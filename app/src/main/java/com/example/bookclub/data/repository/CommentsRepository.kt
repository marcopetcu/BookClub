package com.example.bookclub.data.repository

import com.example.bookclub.data.db.CommentEntity
import com.example.bookclub.data.db.InboxEntity
import com.example.bookclub.data.db.VoteEntity
import com.example.bookclub.data.db.dao.CommentDao
import com.example.bookclub.data.db.dao.InboxDao
import com.example.bookclub.data.db.dao.VoteDao
import com.example.bookclub.data.model.ClubComment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class CommentsRepository(
    private val commentDao: CommentDao,
    private val voteDao: VoteDao,
    private val inboxDao: InboxDao
) {
    /** Top-level comments + autor (nickname/email). */
    fun getComments(clubId: Long): Flow<List<ClubComment>> =
        commentDao.getTopLevelWithAuthor(clubId).map { rows ->
            rows.map { row ->
                ClubComment(
                    id = row.id,
                    clubId = row.clubId,
                    userId = row.userId,
                    authorName = row.authorNickname?.takeIf { it.isNotBlank() }
                        ?: row.authorEmail?.takeIf { it.isNotBlank() }
                        ?: "Anonymous",
                    content = row.content,
                    createdAt = row.createdAt
                )
            }
        }
    fun getRepliesFlow(parentId: Long): Flow<List<ClubComment>> =
        commentDao.getReplies(parentId).map { rows ->
            rows.map { e ->
                ClubComment(
                    id = e.id,
                    clubId = e.clubId,
                    userId = e.userId,
                    authorName = null, // sau mapează cu JOIN dacă vrei autorul
                    content = e.content,
                    createdAt = e.createdAt
                )
            }
        }
    /** Inserare comentariu (sau reply). */
    suspend fun insertComment(
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

    /** Vote (1 / -1). */
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

    /** Ia replicile (o singură dată) cu autor. */
    suspend fun getRepliesOnce(parentId: Long): List<ClubComment> =
        commentDao.getRepliesWithAuthor(parentId).map { row ->
            ClubComment(
                id = row.id,
                clubId = row.clubId,
                userId = row.userId,
                authorName = row.authorNickname?.takeIf { it.isNotBlank() }
                    ?: row.authorEmail?.takeIf { it.isNotBlank() } ?: "Anonymous",
                content = row.content,
                createdAt = row.createdAt
            )
        }

    /** Numărul de replici. */
    suspend fun countReplies(parentId: Long): Int =
        commentDao.countReplies(parentId)
}
