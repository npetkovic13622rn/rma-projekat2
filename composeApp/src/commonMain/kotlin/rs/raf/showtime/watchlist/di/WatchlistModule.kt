package rs.raf.showtime.watchlist.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.raf.showtime.core.network.ApiConstants
import rs.raf.showtime.networking.di.Qualifiers
import rs.raf.showtime.watchlist.data.WatchlistApi
import rs.raf.showtime.watchlist.data.WatchlistRepositoryImpl
import rs.raf.showtime.watchlist.data.createWatchlistApi
import rs.raf.showtime.watchlist.domain.WatchlistRepository
import rs.raf.showtime.watchlist.presentation.WatchlistViewModel

val watchlistModule = module {
    single<WatchlistApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl(ApiConstants.BASE_URL)
            .build()
            .createWatchlistApi()
    }

    single {
        WatchlistRepositoryImpl(
            appDatabase = get(),
            watchlistApi = get(),
        )
    } bind WatchlistRepository::class

    viewModelOf(::WatchlistViewModel)
}
