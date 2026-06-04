package rs.raf.showtime.core.auth

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import rs.raf.showtime.core.auth.model.AuthData
import rs.raf.showtime.core.auth.model.AuthState
import rs.raf.showtime.core.auth.model.asAuthenticationState

class AuthStore(
    private val persistence: DataStore<AuthData>,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val authData = persistence.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first() },
        )

    suspend fun setAuthData(authData: AuthData) =
        persistence.updateData {
            authData
        }

    suspend fun setAccessToken(accessToken: String) =
        persistence.updateData {
            it.copy(accessToken = accessToken)
        }

    suspend fun saveToken(token: String) =
        setAccessToken(accessToken = token)

    fun getTokenFlow() =
        persistence.data
            .map { it.accessToken?.takeIf { token -> token.isNotBlank() } }
            .distinctUntilChanged()

    fun getToken(): String? =
        authData.value.accessToken?.takeIf { it.isNotBlank() }

    suspend fun clearToken() =
        persistence.updateData {
            it.copy(accessToken = null, refreshToken = null)
        }

    suspend fun clearAuthData() =
        persistence.updateData { AuthData.empty() }

    val authState: StateFlow<AuthState> = persistence.data
        .map { it.asAuthenticationState() }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.Unauthenticated
        )

    suspend fun awaitInitialAuthState(): AuthState {
        return persistence.data
            .map { it.asAuthenticationState() }
            .first()
    }


}
