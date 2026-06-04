package rs.raf.showtime.networking.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.SetupRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import rs.raf.showtime.core.auth.AuthSessionManager
import rs.raf.showtime.core.auth.AuthStore
import rs.raf.showtime.core.auth.model.AuthState
import rs.raf.showtime.networking.HttpClientFactory

val networkingModule = module {

    single<HttpClient>(Qualifiers.Unauthenticated) {
        HttpClientFactory.createHttpClientWithDefaultConfig()
    }

    single<HttpClient>(Qualifiers.Authenticated) {
        val authStoreLazy: Lazy<AuthStore> = inject()
        val authSessionManagerLazy: Lazy<AuthSessionManager> = inject()
        HttpClientFactory.createHttpClientWithDefaultConfig {
            installAuthPlugin(
                authStoreLazy = authStoreLazy,
                authSessionManagerLazy = authSessionManagerLazy,
            )
        }
    }
}

/**
 * Installs auth plugin that:
 * 1. Adds bearer token to requests
 * 2. Emits a forced logout event on 401 Unauthorized
 */
private fun HttpClientConfig<*>.installAuthPlugin(
    authStoreLazy: Lazy<AuthStore>,
    authSessionManagerLazy: Lazy<AuthSessionManager>,
) = install(createClientPlugin("AuthPlugin") {

    on(SetupRequest) { request ->
        val authStore = authStoreLazy.value
        when (val authState = authStore.authState.value) {
            is AuthState.Authenticated -> {
                request.header(
                    key = HttpHeaders.Authorization,
                    value = "Bearer ${authState.data.accessToken}",
                )
            }
            AuthState.CheckingSession -> Unit
            AuthState.Unauthenticated -> Unit
        }
    }

    on(Send) { request ->
        val originalCall = proceed(request)

        originalCall.response.run {
            if (status != HttpStatusCode.Unauthorized) {
                return@run originalCall
            }

            val newAuthState = runBlocking {
                authSessionManagerLazy.value.requestForceLogout()
                AuthState.Unauthenticated
            }

            @Suppress("IMPOSSIBLE_IS_CHECK_WARNING")
            when (newAuthState) {
                is AuthState.Authenticated -> {
                    // Refresh uspeo - ponavljamo zahtev sa novim tokenom
                    request.headers.remove(name = HttpHeaders.Authorization)
                    request.headers.append(
                        name = HttpHeaders.Authorization,
                        value = "Bearer ${newAuthState.data.accessToken}",
                    )
                    proceed(request)
                }
                AuthState.CheckingSession,
                AuthState.Unauthenticated -> {
                    originalCall
                }
            }
        }
    }
})
