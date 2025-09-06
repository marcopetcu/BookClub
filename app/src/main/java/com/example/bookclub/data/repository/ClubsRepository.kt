package com.example.bookclub.data.repository

import com.example.bookclub.data.db.*
import com.example.bookclub.data.db.dao.*
import com.example.bookclub.data.model.ClubStatus
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant

class ClubsRepository(
    private val clubDao: BookClubDao,
    private val membershipDao: MembershipDao,
    private val followBookDao: FollowBookDao,
    private val inboxDao: InboxDao
) {
    fun listAll(): Flow<List<BookClubEntity>> = clubDao.getAllOrderByStart()

    fun search(query: String): Flow<List<BookClubEntity>> =
        clubDao.search("%$query%")

    fun listForUser(userId: Long): Flow<List<BookClubEntity>> =
        membershipDao.getClubsForUser(userId)

    fun listForFollowedBooks(userId: Long): Flow<List<BookClubEntity>> =
        clubDao.listForFollowedBooks(userId)

    suspend fun createClub(
        adminId: Long,
        workId: String,
        title: String,
        author: String,
        coverUrl: String?,
        description: String?,
        startAt: Instant
    ): Long {
        val now = Instant.now()
        val status = if (startAt.isAfter(now)) ClubStatus.SCHEDULED else ClubStatus.LIVE
        val closeAt = startAt.plus(Duration.ofHours(72))

        if (clubDao.existsActiveForWork(workId)) {
            throw IllegalStateException("Active club already exists for this work")
        }

        val id = clubDao.upsert(
            BookClubEntity(
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

        val followers: List<Long> = followBookDao.getFollowerIdsForWork(workId)
        val nowTs = Instant.now()
        followers.forEach { uid ->
            inboxDao.insert(
                InboxEntity(
                    userId = uid,
                    type = "NEW_CLUB_FOR_FOLLOWED_BOOK",
                    payloadJson = """{"clubId":$id,"workId":"$workId","title":"$title"}""",
                    isRead = false,
                    createdAt = nowTs
                )
            )
        }
        return id
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
}
