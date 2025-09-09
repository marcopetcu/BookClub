package com.example.bookclub.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.bookclub.data.db.BookClubEntity   // <— IMPORTUL LIPSEA
import com.example.bookclub.data.model.ClubStatus
import kotlinx.coroutines.flow.Flow

// DAO Room pentru BookClub: metode CRUD si query-uri specifice
@Dao
interface BookClubDao {

    /** Insert nou – întoarce id-ul generat */
    @Insert
    suspend fun insert(club: BookClubEntity): Long

    /** Upsert pentru update-uri ulterioare (nu te baza pe return) */
    @Upsert
    suspend fun upsert(club: BookClubEntity)

    /** Toate cluburile ordonate după start */
    @Query("SELECT * FROM bookclub ORDER BY startAt ASC")
    fun getAllOrderByStart(): Flow<List<BookClubEntity>>

    /** Căutare după titlu/autor – wildcard în SQL */
    @Query("""
        SELECT * FROM bookclub
        WHERE title  LIKE '%' || :q || '%'
           OR author LIKE '%' || :q || '%'
        ORDER BY startAt ASC
    """)
    fun search(q: String): Flow<List<BookClubEntity>>

    /** Cluburi pentru o carte – LIVE primele, apoi SCHEDULED, apoi CLOSED */
    @Query("""
        SELECT * FROM bookclub
        WHERE workId = :workId
        ORDER BY 
          CASE status 
            WHEN 'LIVE' THEN 0 
            WHEN 'SCHEDULED' THEN 1 
            ELSE 2 
          END,
          startAt ASC
    """)
    fun clubsForWork(workId: String): Flow<List<BookClubEntity>>

    /** Există deja un club activ (SCHEDULED/LIVE) pentru cartea dată? */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM bookclub 
            WHERE workId = :workId 
              AND status IN (:active1, :active2)
        )
    """)
    suspend fun existsActiveForWork(
        workId: String,
        active1: ClubStatus = ClubStatus.SCHEDULED,
        active2: ClubStatus = ClubStatus.LIVE
    ): Boolean

    /** Club după id */
    @Query("SELECT * FROM bookclub WHERE id = :id")
    suspend fun getById(id: Long): BookClubEntity?

    @Query("SELECT * FROM bookclub WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Long): kotlinx.coroutines.flow.Flow<BookClubEntity?>


    /** Upcoming/LIVE pentru cărțile urmărite (JOIN cu follow_book) */
    @Query("""
        SELECT bc.* FROM bookclub bc
        JOIN follow_book fb ON fb.workId = bc.workId
        WHERE fb.userId = :userId 
          AND bc.status IN (:s1, :s2)
        ORDER BY 
          CASE bc.status WHEN 'LIVE' THEN 0 ELSE 1 END,
          bc.startAt ASC
    """)


    fun listForFollowedBooks(
        userId: Long,
        s1: ClubStatus = ClubStatus.SCHEDULED,
        s2: ClubStatus = ClubStatus.LIVE
    ): Flow<List<BookClubEntity>>
}
