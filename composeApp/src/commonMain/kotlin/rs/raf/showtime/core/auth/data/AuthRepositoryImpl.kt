package rs.raf.showtime.core.auth.data

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rs.raf.showtime.core.auth.AuthSessionManager
import rs.raf.showtime.core.auth.AuthStore
import rs.raf.showtime.core.auth.domain.AuthException
import rs.raf.showtime.core.auth.domain.AuthRepository
import rs.raf.showtime.core.auth.model.AuthData
import rs.raf.showtime.core.auth.model.AuthState
import rs.raf.showtime.core.auth.remote.AuthApi
import rs.raf.showtime.core.auth.remote.dto.LoginRequestDto
import rs.raf.showtime.core.auth.remote.dto.SignupRequestDto
import rs.raf.showtime.core.db.AppDatabase
import rs.raf.showtime.profile.data.local.entity.LocalUserEntity
import rs.raf.showtime.profile.data.remote.dto.UserDto

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val authStore: AuthStore,
    private val appDatabase: AppDatabase,
    authSessionManager: AuthSessionManager,
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            authSessionManager.forceLogoutEvents.collect {
                forceLogout()
            }
        }
    }

    override fun observeAuthState(): Flow<AuthState> =
        authStore.authState
            .onStart { emit(AuthState.CheckingSession) }
            .distinctUntilChanged()

    override suspend fun login(username: String, password: String) {
        runAuthCatching(loginMessage = "Invalid username or password.") {
            val response = authApi.login(
                LoginRequestDto(
                    username = username.trim(),
                    password = password,
                ),
            )
            authStore.setAuthData(
                AuthData(
                    accessToken = response.accessToken,
                ),
            )
            response.user?.let { appDatabase.profileDao().upsertLocalUser(it.toLocalUserEntity()) }
        }
    }

    override suspend fun signup(fullName: String, username: String, password: String) {
        runAuthCatching(loginMessage = "Unable to create account.") {
            val response = authApi.signup(
                SignupRequestDto(
                    fullName = fullName.trim(),
                    username = username.trim(),
                    password = password,
                ),
            )
            authStore.setAuthData(
                AuthData(
                    accessToken = response.accessToken,
                ),
            )
            response.user?.let { appDatabase.profileDao().upsertLocalUser(it.toLocalUserEntity()) }
        }
    }

    override suspend fun logout() {
        clearSession()
    }

    override suspend fun forceLogout() {
        clearSession()
    }

    private suspend fun clearSession() {
        authStore.clearToken()
        appDatabase.favoritesDao().clearFavorites()
        appDatabase.watchlistDao().clearWatchlist()
        appDatabase.profileDao().clearLocalUser()
    }

    private suspend fun runAuthCatching(
        loginMessage: String,
        block: suspend () -> Unit,
    ) {
        try {
            block()
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            throw AuthException(error.toAuthMessage(defaultMessage = loginMessage), error)
        }
    }

    private fun Throwable.toAuthMessage(defaultMessage: String): String =
        when (this) {
            is ClientRequestException -> when (response.status) {
                HttpStatusCode.Unauthorized -> "Invalid username or password."
                HttpStatusCode.Conflict -> "Username is already taken."
                HttpStatusCode.BadRequest -> "Please check the entered data."
                else -> defaultMessage
            }
            is ServerResponseException -> "Server is unavailable. Try again later."
            is ResponseException -> defaultMessage
            else -> message ?: "Network error. Check your connection."
        }

    private fun UserDto.toLocalUserEntity(): LocalUserEntity =
        LocalUserEntity(
            remoteId = id?.toString(),
            fullName = fullName,
            username = username,
            updatedAt = null,
        )
}
