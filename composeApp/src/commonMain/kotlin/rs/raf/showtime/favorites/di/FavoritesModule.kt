package rs.raf.showtime.favorites.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.raf.showtime.core.network.ApiConstants
import rs.raf.showtime.favorites.data.FavoritesApi
import rs.raf.showtime.favorites.data.FavoritesRepositoryImpl
import rs.raf.showtime.favorites.data.createFavoritesApi
import rs.raf.showtime.favorites.domain.FavoritesRepository
import rs.raf.showtime.favorites.presentation.FavoritesViewModel
import rs.raf.showtime.networking.di.Qualifiers

val favoritesModule = module {
    single<FavoritesApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl(ApiConstants.BASE_URL)
            .build()
            .createFavoritesApi()
    }

    single {
        FavoritesRepositoryImpl(
            appDatabase = get(),
            favoritesApi = get(),
        )
    } bind FavoritesRepository::class

    viewModelOf(::FavoritesViewModel)
}
