package rs.raf.showtime.movies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MovieListItemDto(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Double? = null,
    val imdbVotes: Int? = null,
    val runtime: Int? = null,
    val posterPath: String? = null,
    val genres: List<GenreDto> = emptyList(),
)
