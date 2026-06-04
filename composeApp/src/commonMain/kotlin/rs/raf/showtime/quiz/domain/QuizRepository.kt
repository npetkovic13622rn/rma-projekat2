package rs.raf.showtime.quiz.domain

import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    suspend fun canStartQuiz(): Boolean
    suspend fun getAvailableMovieCount(): Int
    suspend fun startQuiz(): QuizSession
    suspend fun saveQuizResult(result: QuizResult)
    fun observeQuizStats(): Flow<QuizStats>
    suspend fun postQuizResultToLeaderboard(score: Double, category: Int = QuizRules.Category)
    fun observeLatestQuizResult(): Flow<QuizResult?>
}
