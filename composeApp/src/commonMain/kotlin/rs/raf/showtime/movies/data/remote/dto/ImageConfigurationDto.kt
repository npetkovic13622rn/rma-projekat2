package rs.raf.showtime.movies.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfigEntryDto(
    val key: String,
    val value: String,
)

@Serializable
data class ImageConfigurationDto(
    @SerialName("image_base_url")
    val imageBaseUrl: String? = null,
    @SerialName("poster_sizes")
    val posterSizes: List<String> = emptyList(),
    @SerialName("backdrop_sizes")
    val backdropSizes: List<String> = emptyList(),
    @SerialName("profile_sizes")
    val profileSizes: List<String> = emptyList(),
    @SerialName("logo_sizes")
    val logoSizes: List<String> = emptyList(),
)
