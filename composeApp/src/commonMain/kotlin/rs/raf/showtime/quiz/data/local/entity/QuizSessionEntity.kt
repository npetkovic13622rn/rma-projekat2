package rs.raf.showtime.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Double,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val usedTimeSeconds: Int,
    val createdAt: Long,
)
