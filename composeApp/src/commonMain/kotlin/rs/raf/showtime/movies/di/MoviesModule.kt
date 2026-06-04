package rs.raf.showtime.movies.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.raf.showtime.core.network.ApiConstants
import rs.raf.showtime.movies.data.remote.MoviesApi
import rs.raf.showtime.movies.data.remote.createMoviesApi
import rs.raf.showtime.movies.data.repository.MoviesRepositoryImpl
import rs.raf.showtime.movies.domain.MoviesRepository
import rs.raf.showtime.movies.presentation.details.MovieDetailsViewModel
import rs.raf.showtime.movies.presentation.list.MoviesListViewModel
import rs.raf.showtime.networking.di.Qualifiers

val moviesModule = module {
    single<MoviesApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Unauthenticated))
            .baseUrl(ApiConstants.BASE_URL)
            .build()
            .createMoviesApi()
    }

    single { MoviesRepositoryImpl(appDatabase = get(), moviesApi = get()) } bind MoviesRepository::class

    viewModelOf(::MoviesListViewModel)
    viewModelOf(::MovieDetailsViewModel)
}
