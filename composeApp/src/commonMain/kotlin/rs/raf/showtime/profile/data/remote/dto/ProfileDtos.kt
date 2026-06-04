package rs.raf.showtime.profile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long? = null,
    @SerialName("full_name")
    val fullName: String,
    val username: String,
)
