package rs.raf.showtime.quiz.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizResultRequestDto(
    val score: Double,
    val category: Int = 1,
)

@Serializable
data class QuizResultDto(
    val id: Long? = null,
    val category: Int,
    val score: Double,
    @SerialName("played_at")
    val playedAt: Long,
)

@Serializable
data class LeaderboardEntryDto(
    val rank: Int,
    @SerialName("user_id")
    val userId: Long? = null,
    val username: String,
    @SerialName("full_name")
    val fullName: String? = null,
    val score: Double,
    @SerialName("played_at")
    val playedAt: Long,
    @SerialName("total_plays")
    val totalPlays: Int,
)

@Serializable
data class PostQuizResultResponseDto(
    val result: QuizResultDto,
    val ranking: Int,
)
