package rs.raf.showtime.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_stats")
data class QuizStatsEntity(
    @PrimaryKey val id: Int = 0,
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
    val lastPlayedAt: Long? = null,
)
