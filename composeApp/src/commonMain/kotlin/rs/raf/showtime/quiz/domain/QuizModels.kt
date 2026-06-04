package rs.raf.showtime.quiz.domain

data class QuizAnswer(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
)

data class QuizQuestion(
    val id: String,
    val type: QuizQuestionType,
    val movieId: String,
    val prompt: String,
    val imagePath: String?,
    val answers: List<QuizAnswer>,
    val correctAnswerId: String,
)

enum class QuizQuestionType {
    GuessMovie,
    GuessYear,
    GuessLeadActor,
}

data class QuizSession(
    val id: String,
    val questions: List<QuizQuestion>,
    val totalSeconds: Int = QuizRules.TotalSeconds,
)

data class QuizResult(
    val id: Long = 0,
    val score: Double,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val usedTimeSeconds: Int,
    val createdAt: Long,
)

data class QuizStats(
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
    val lastPlayedAt: Long? = null,
)

class QuizException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

object QuizRules {
    const val Category = 1
    const val QuestionCount = 10
    const val TotalSeconds = 60
    const val MaxPerType = 4

    fun calculateScore(
        correctAnswers: Int,
        remainingSeconds: Int,
    ): Double {
        val rawScore = correctAnswers * (9 + remainingSeconds.coerceIn(0, TotalSeconds) / 60.0)
        return rawScore.coerceIn(0.0, 100.0)
    }
}
