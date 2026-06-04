package rs.raf.showtime.core.auth.di

import androidx.datastore.core.DataStore
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.raf.showtime.core.auth.AuthSessionManager
import rs.raf.showtime.core.auth.AuthStore
import rs.raf.showtime.core.auth.createAuthDataStore
import rs.raf.showtime.core.auth.data.AuthRepositoryImpl
import rs.raf.showtime.core.auth.domain.AuthRepository
import rs.raf.showtime.core.auth.model.AuthData
import rs.raf.showtime.core.auth.presentation.landing.AuthLandingViewModel
import rs.raf.showtime.core.auth.presentation.login.LoginViewModel
import rs.raf.showtime.core.auth.presentation.root.AuthRootViewModel
import rs.raf.showtime.core.auth.presentation.signup.SignupViewModel
import rs.raf.showtime.core.auth.remote.AuthApi
import rs.raf.showtime.core.auth.remote.createAuthApi
import rs.raf.showtime.core.network.ApiConstants
import rs.raf.showtime.networking.di.Qualifiers

val authModule = module {

    single<DataStore<AuthData>> { createAuthDataStore() }

    single<AuthStore> { AuthStore(persistence = get()) }

    single { AuthSessionManager() }

    single<AuthApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Unauthenticated))
            .baseUrl(ApiConstants.BASE_URL)
            .build()
            .createAuthApi()
    }

    single {
        AuthRepositoryImpl(
            authApi = get(),
            authStore = get(),
            appDatabase = get(),
            authSessionManager = get(),
        )
    } bind AuthRepository::class

    viewModelOf(::AuthRootViewModel)
    viewModelOf(::AuthLandingViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
}
