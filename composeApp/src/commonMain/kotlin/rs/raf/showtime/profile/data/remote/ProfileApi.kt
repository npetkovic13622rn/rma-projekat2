package rs.raf.showtime.profile.data.remote

import de.jensklingenberg.ktorfit.http.GET
import rs.raf.showtime.profile.data.remote.dto.UserDto

interface ProfileApi {

    @GET("me")
    suspend fun getMe(): UserDto
}
