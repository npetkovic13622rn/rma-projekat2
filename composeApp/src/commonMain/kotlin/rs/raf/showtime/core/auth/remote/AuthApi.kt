package rs.raf.showtime.core.auth.remote

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import rs.raf.showtime.core.auth.remote.dto.AuthResponseDto
import rs.raf.showtime.core.auth.remote.dto.LoginRequestDto
import rs.raf.showtime.core.auth.remote.dto.SignupRequestDto

interface AuthApi {

    @POST("auth/signup")
    suspend fun signup(
        @Body body: SignupRequestDto,
    ): AuthResponseDto

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequestDto,
    ): AuthResponseDto
}
