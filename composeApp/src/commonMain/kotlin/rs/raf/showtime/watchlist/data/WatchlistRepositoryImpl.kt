package rs.raf.showtime.watchlist.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import rs.raf.showtime.core.db.AppDatabase
import rs.raf.showtime.core.util.RepositoryException
import rs.raf.showtime.movies.data.local.dao.MoviesDao
import rs.raf.showtime.movies.data.mapper.toDomain
import rs.raf.showtime.movies.data.mapper.toGenreCrossRefs
import rs.raf.showtime.movies.data.mapper.toGenreEntity
import rs.raf.showtime.movies.data.mapper.toMovieEntity
import rs.raf.showtime.movies.domain.Movie
import rs.raf.showtime.watchlist.data.local.dao.WatchlistDao
import rs.raf.showtime.watchlist.data.local.entity.WatchlistEntity
import rs.raf.showtime.watchlist.domain.WatchlistRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class WatchlistRepositoryImpl(
    appDatabase: AppDatabase,
    private val watchlistApi: WatchlistApi,
) : WatchlistRepository {

    private val watchlistDao: WatchlistDao = appDatabase.watchlistDao()
    private val moviesDao: MoviesDao = appDatabase.moviesDao()

    override fun observeWatchlistMovies(): Flow<List<Movie>> =
        watchlistDao.observeWatchlistMovies()
            .distinctUntilChanged()
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeWatchlistMovieIds(): Flow<Set<String>> =
        watchlistDao.observeWatchlistMovieIds()
            .distinctUntilChanged()
            .map { ids -> ids.toSet() }

    override fun observeIsInWatchlist(movieId: String): Flow<Boolean> =
        watchlistDao.isInWatchlist(movieId).distinctUntilChanged()

    override suspend fun syncWatchlist() {
        runRepositoryCatching("Failed to sync watchlist.") {
            val remoteMovies = watchlistApi.getWatchlist()
            moviesDao.refreshMoviesTransaction(
                movies = remoteMovies.map { it.toMovieEntity() },
                genres = remoteMovies
                    .flatMap { it.genres }
                    .distinctBy { it.id }
                    .map { it.toGenreEntity() },
                crossRefs = remoteMovies.flatMap { it.toGenreCrossRefs() },
            )
            watchlistDao.replaceWatchlist(
                remoteMovies.map { movie ->
                    WatchlistEntity(
                        movieId = movie.imdbId,
                        createdAt = currentTimestamp(),
                    )
                },
            )
        }
    }

    override suspend fun toggleWatchlist(movieId: String) {
        val current = watchlistDao.getWatchlist(movieId)
        if (current == null) {
            addToWatchlistOptimistically(movieId)
        } else {
            removeFromWatchlist(movieId)
        }
    }

    override suspend fun removeFromWatchlist(movieId: String) {
        val previous = watchlistDao.getWatchlist(movieId)
        watchlistDao.deleteWatchlist(movieId)

        runCatching {
            watchlistApi.removeFromWatchlist(movieId)
        }.onFailure { error ->
            if (error is CancellationException) throw error
            previous?.let { watchlistDao.upsertWatchlist(it) }
            throw RepositoryException("Failed to remove from watchlist.", error)
        }
    }

    override suspend fun clearWatchlistLocal() {
        watchlistDao.clearWatchlist()
    }

    private suspend fun addToWatchlistOptimistically(movieId: String) {
        val optimistic = WatchlistEntity(
            movieId = movieId,
            createdAt = currentTimestamp(),
        )
        watchlistDao.upsertWatchlist(optimistic)

        runCatching {
            watchlistApi.addToWatchlist(movieId)
        }.onFailure { error ->
            if (error is CancellationException) throw error
            watchlistDao.deleteWatchlist(movieId)
            throw RepositoryException("Failed to add to watchlist.", error)
        }
    }

    private suspend fun runRepositoryCatching(
        message: String,
        block: suspend () -> Unit,
    ) {
        try {
            block()
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            throw RepositoryException(message, error)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun currentTimestamp(): Long =
        Clock.System.now().toEpochMilliseconds()
}
