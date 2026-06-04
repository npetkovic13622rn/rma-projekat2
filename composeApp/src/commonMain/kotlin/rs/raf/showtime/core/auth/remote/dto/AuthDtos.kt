package rs.raf.showtime.core.auth.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import rs.raf.showtime.profile.data.remote.dto.UserDto

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
)

@Serializable
data class SignupRequestDto(
    @SerialName("full_name")
    val fullName: String,
    val username: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Long? = null,
    val user: UserDto? = null,
)
