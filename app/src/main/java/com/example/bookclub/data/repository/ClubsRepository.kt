package com.example.bookclub.data.repository

import com.example.bookclub.data.db.*
import com.example.bookclub.data.db.dao.*
import com.example.bookclub.data.model.ClubComment
import com.example.bookclub.data.model.ClubStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant

class ClubsRepository(
    private val clubDao: BookClubDao,
    private val membershipDao: MembershipDao,
    private val followBookDao: FollowBookDao,
    private val inboxDao: InboxDao,
    private val commentDao: CommentDao
) {
    fun listAll(): Flow<List<BookClubEntity>> = clubDao.getAllOrderByStart()
    fun search(query: String): Flow<List<BookClubEntity>> = clubDao.search(query)
    fun listForUser(userId: Long): Flow<List<BookClubEntity>> =
        membershipDao.getClubsForUser(userId)
    fun listForFollowedBooks(userId: Long): Flow<List<BookClubEntity>> =
        clubDao.listForFollowedBooks(userId)

    // ✅ NOU: Flow cu setul clubId-urilor unde userul e membru (pentru UI)
    fun membershipsForUser(userId: Long): Flow<Set<Long>> =
        membershipDao.getClubIdsForUser(userId).map { it.toSet() }

    // opțional, dacă îți mai trebuie punctual
    suspend fun isMember(userId: Long, clubId: Long) = membershipDao.isMember(userId, clubId)

    suspend fun createClub(
        adminId: Long,
        workId: String,
        title: String,
        author: String,
        coverUrl: String?,
        description: String?,
        startAt: Instant
    ): Long {
        if (clubDao.existsActiveForWork(workId)) {
            throw IllegalStateException("Active club already exists for this work")
        }
        val now = Instant.now()
        val status = if (startAt.isAfter(now)) ClubStatus.SCHEDULED else ClubStatus.LIVE
        val closeAt = startAt.plus(Duration.ofHours(72))
        val clubId = clubDao.insert(
            BookClubEntity(
                id = 0L,
                workId = workId,
                title = title,
                author = author,
                coverUrl = coverUrl,
                description = description,
                createdBy = adminId,
                status = status,
                startAt = startAt,
                closeAt = closeAt
            )
        )
        val followers = followBookDao.getFollowerIdsForWork(workId)
        followers.forEach { uid ->
            inboxDao.insert(
                InboxEntity(
                    userId = uid,
                    type = "NEW_CLUB_FOR_FOLLOWED_BOOK",
                    payloadJson = """{"clubId":$clubId,"workId":"$workId","title":"$title"}""",
                    isRead = false,
                    createdAt = now
                )
            )
        }
        return clubId
    }

    suspend fun joinClub(userId: Long, clubId: Long) {
        membershipDao.upsert(MembershipEntity(clubId = clubId, userId = userId))
        inboxDao.insert(
            InboxEntity(
                userId = userId,
                type = "JOIN_CONFIRMED",
                payloadJson = """{"clubId":$clubId}""",
                isRead = false,
                createdAt = Instant.now()
            )
        )
    }

    suspend fun leaveClub(userId: Long, clubId: Long) {
        membershipDao.delete(userId, clubId)
    }

    /* ===== comentarii – las exact cum le ai ===== */
    fun commentsFlow(clubId: Long): Flow<List<ClubComment>> =
        commentDao.getTopLevelWithAuthor(clubId).map { list ->
            list.map { e ->
                ClubComment(
                    id = e.id,
                    clubId = e.clubId,
                    userId = e.userId,
                    content = e.content,
                    authorName = e.authorNickname?.takeIf { it.isNotBlank() } ?: e.authorEmail,
                    createdAt = e.createdAt
                )
            }
        }


    suspend fun addComment(
        clubId: Long,
        userId: Long,
        content: String,
        parentId: Long? = null
    ) {
        commentDao.insert(
            CommentEntity(
                id = 0L,
                clubId = clubId,
                userId = userId,
                content = content,
                createdAt = Instant.now(),
                parentId = parentId
            )
        )
    }
}
