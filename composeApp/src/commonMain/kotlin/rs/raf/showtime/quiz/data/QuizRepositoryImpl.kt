package rs.raf.showtime.quiz.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import rs.raf.showtime.core.db.AppDatabase
import rs.raf.showtime.core.util.RepositoryException
import rs.raf.showtime.movies.data.local.dao.MoviesDao
import rs.raf.showtime.quiz.data.generator.QuizGenerator
import rs.raf.showtime.quiz.data.local.dao.QuizDao
import rs.raf.showtime.quiz.data.local.entity.QuizSessionEntity
import rs.raf.showtime.quiz.data.remote.dto.QuizResultRequestDto
import rs.raf.showtime.quiz.domain.QuizException
import rs.raf.showtime.quiz.domain.QuizRepository
import rs.raf.showtime.quiz.domain.QuizResult
import rs.raf.showtime.quiz.domain.QuizRules
import rs.raf.showtime.quiz.domain.QuizSession
import rs.raf.showtime.quiz.domain.QuizStats

class QuizRepositoryImpl(
    appDatabase: AppDatabase,
    private val quizApi: QuizApi,
    private val quizGenerator: QuizGenerator,
) : QuizRepository {

    private val moviesDao: MoviesDao = appDatabase.moviesDao()
    private val quizDao: QuizDao = appDatabase.quizDao()

    override suspend fun canStartQuiz(): Boolean =
        moviesDao.countQuizCandidateMovies() >= QuizRules.QuestionCount

    override suspend fun getAvailableMovieCount(): Int =
        moviesDao.countQuizCandidateMovies()

    override suspend fun startQuiz(): QuizSession {
        return runRepositoryCatching("Unable to start quiz.") {
            val movies = moviesDao.getQuizCandidateMovies()
            val imagesByMovie = movies.associate { movie ->
                movie.movieId to moviesDao.getQuizImagesForMovie(movie.movieId)
            }
            val castByMovie = movies.associate { movie ->
                movie.movieId to moviesDao.getQuizCastForMovie(movie.movieId)
            }
            val allCast = moviesDao.getAllQuizCastMembers()

            quizGenerator.generate(
                movies = movies,
                imagesByMovie = imagesByMovie,
                castByMovie = castByMovie,
                allCast = allCast,
            )
        }
    }

    override suspend fun saveQuizResult(result: QuizResult) {
        runRepositoryCatching("Failed to save quiz result.") {
            val sessionId = quizDao.insertQuizSession(result.toEntity())
            quizDao.updateBestScoreAndPlayedCount(
                score = result.score,
                playedAt = result.createdAt,
            )
            runCatching {
                postQuizResultToLeaderboard(score = result.score)
            }
            sessionId
        }
    }

    override fun observeQuizStats(): Flow<QuizStats> =
        quizDao.observeQuizStats()
            .distinctUntilChanged()
            .map { stats ->
                QuizStats(
                    bestScore = stats?.bestScore ?: 0.0,
                    playedCount = stats?.playedCount ?: 0,
                    lastPlayedAt = stats?.lastPlayedAt,
                )
            }

    override suspend fun postQuizResultToLeaderboard(score: Double, category: Int) {
        quizApi.submitLeaderboardResult(
            QuizResultRequestDto(
                score = score,
                category = category,
            ),
        )
    }

    override fun observeLatestQuizResult(): Flow<QuizResult?> =
        quizDao.observeLatestQuizSession()
            .distinctUntilChanged()
            .map { it?.toDomain() }

    private suspend fun <T> runRepositoryCatching(
        message: String,
        block: suspend () -> T,
    ): T {
        try {
            return block()
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            if (error is QuizException) throw error
            throw RepositoryException(message, error)
        }
    }

    private fun QuizResult.toEntity(): QuizSessionEntity =
        QuizSessionEntity(
            score = score,
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            usedTimeSeconds = usedTimeSeconds,
            createdAt = createdAt,
        )

    private fun QuizSessionEntity.toDomain(): QuizResult =
        QuizResult(
            id = id,
            score = score,
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            usedTimeSeconds = usedTimeSeconds,
            createdAt = createdAt,
        )
}
