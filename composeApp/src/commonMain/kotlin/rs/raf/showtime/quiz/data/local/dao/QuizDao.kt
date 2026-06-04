package rs.raf.showtime.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.raf.showtime.quiz.data.local.entity.QuizSessionEntity
import rs.raf.showtime.quiz.data.local.entity.QuizStatsEntity

@Dao
interface QuizDao {

    @Query("SELECT * FROM quiz_stats WHERE id = 0")
    fun observeQuizStats(): Flow<QuizStatsEntity?>

    @Query("SELECT * FROM quiz_sessions ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestQuizSession(): Flow<QuizSessionEntity?>

    @Query("SELECT * FROM quiz_stats WHERE id = 0")
    suspend fun getQuizStats(): QuizStatsEntity?

    @Upsert
    suspend fun upsertQuizStats(stats: QuizStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizSession(session: QuizSessionEntity): Long

    @Query("DELETE FROM quiz_stats")
    suspend fun clearQuizStats()

    @Query("DELETE FROM quiz_sessions")
    suspend fun clearQuizSessions()

    @Transaction
    suspend fun updateBestScoreAndPlayedCount(score: Double, playedAt: Long) {
        val current = getQuizStats()
        upsertQuizStats(
            current?.copy(
                bestScore = maxOf(current.bestScore, score),
                playedCount = current.playedCount + 1,
                lastPlayedAt = playedAt,
            ) ?: QuizStatsEntity(
                bestScore = score,
                playedCount = 1,
                lastPlayedAt = playedAt,
            ),
        )
    }
}
