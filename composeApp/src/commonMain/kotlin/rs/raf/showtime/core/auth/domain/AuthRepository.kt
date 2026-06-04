package rs.raf.showtime.core.auth.domain

import kotlinx.coroutines.flow.Flow
import rs.raf.showtime.core.auth.model.AuthState

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun login(username: String, password: String)
    suspend fun signup(fullName: String, username: String, password: String)
    suspend fun logout()
    suspend fun forceLogout()
}
