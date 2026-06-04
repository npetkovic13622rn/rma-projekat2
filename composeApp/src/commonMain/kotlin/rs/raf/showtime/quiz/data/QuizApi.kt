package rs.raf.showtime.quiz.data

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import rs.raf.showtime.quiz.data.remote.dto.LeaderboardEntryDto
import rs.raf.showtime.quiz.data.remote.dto.PostQuizResultResponseDto
import rs.raf.showtime.quiz.data.remote.dto.QuizResultDto
import rs.raf.showtime.quiz.data.remote.dto.QuizResultRequestDto

interface QuizApi {

    @GET("me/quiz-results")
    suspend fun getMyQuizResults(): List<QuizResultDto>

    @POST("leaderboard")
    suspend fun submitLeaderboardResult(
        @Body body: QuizResultRequestDto,
    ): PostQuizResultResponseDto

    @GET("leaderboard")
    suspend fun getLeaderboard(): List<LeaderboardEntryDto>
}
