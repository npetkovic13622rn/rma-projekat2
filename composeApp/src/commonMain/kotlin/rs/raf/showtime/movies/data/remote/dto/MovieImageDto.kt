package rs.raf.showtime.movies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MovieImageDto(
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    val voteAverage: Double? = null,
    val language: String? = null,
)
