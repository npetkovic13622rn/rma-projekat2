package rs.raf.showtime.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import rs.raf.showtime.core.auth.di.authModule
import rs.raf.showtime.core.db.di.databaseModule
import rs.raf.showtime.favorites.di.favoritesModule
import rs.raf.showtime.movies.di.moviesModule
import rs.raf.showtime.networking.di.networkingModule
import rs.raf.showtime.quiz.di.quizModule
import rs.raf.showtime.watchlist.di.watchlistModule

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        config?.invoke(this)
        modules(
            databaseModule(),
            networkingModule,
            authModule,
            moviesModule,
            favoritesModule,
            watchlistModule,
            quizModule,
        )
    }
}
