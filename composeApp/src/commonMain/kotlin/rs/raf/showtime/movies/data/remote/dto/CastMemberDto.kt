package rs.raf.showtime.movies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CastMemberDto(
    val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    val profilePath: String? = null,
)
