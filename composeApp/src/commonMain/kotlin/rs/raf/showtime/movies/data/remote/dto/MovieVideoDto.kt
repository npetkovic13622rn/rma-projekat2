package rs.raf.showtime.movies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MovieVideoDto(
    val key: String,
    val site: String,
    val name: String? = null,
    val type: String? = null,
    val official: Boolean = false,
    val publishedAt: String? = null,
)
