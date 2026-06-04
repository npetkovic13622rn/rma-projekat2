package rs.raf.showtime.movies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MovieImagesDto(
    val posters: List<MovieImageDto> = emptyList(),
    val backdrops: List<MovieImageDto> = emptyList(),
    val logos: List<MovieImageDto> = emptyList(),
)
