package rs.raf.showtime.favorites.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import rs.raf.showtime.core.db.AppDatabase
import rs.raf.showtime.core.util.RepositoryException
import rs.raf.showtime.favorites.data.local.dao.FavoritesDao
import rs.raf.showtime.favorites.data.local.entity.FavoriteEntity
import rs.raf.showtime.favorites.domain.FavoritesRepository
import rs.raf.showtime.movies.data.local.dao.MoviesDao
import rs.raf.showtime.movies.data.mapper.toDomain
import rs.raf.showtime.movies.data.mapper.toGenreCrossRefs
import rs.raf.showtime.movies.data.mapper.toGenreEntity
import rs.raf.showtime.movies.data.mapper.toMovieEntity
import rs.raf.showtime.movies.domain.Movie
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FavoritesRepositoryImpl(
    appDatabase: AppDatabase,
    private val favoritesApi: FavoritesApi,
) : FavoritesRepository {

    private val favoritesDao: FavoritesDao = appDatabase.favoritesDao()
    private val moviesDao: MoviesDao = appDatabase.moviesDao()

    override fun observeFavoriteMovies(): Flow<List<Movie>> =
        favoritesDao.observeFavoriteMovies()
            .distinctUntilChanged()
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeFavoriteMovieIds(): Flow<Set<String>> =
        favoritesDao.observeFavoriteMovieIds()
            .distinctUntilChanged()
            .map { ids -> ids.toSet() }

    override fun observeIsFavorite(movieId: String): Flow<Boolean> =
        favoritesDao.isFavorite(movieId).distinctUntilChanged()

    override suspend fun syncFavorites() {
        runRepositoryCatching("Failed to sync favorites.") {
            val remoteMovies = favoritesApi.getFavorites()
            moviesDao.refreshMoviesTransaction(
                movies = remoteMovies.map { it.toMovieEntity() },
                genres = remoteMovies
                    .flatMap { it.genres }
                    .distinctBy { it.id }
                    .map { it.toGenreEntity() },
                crossRefs = remoteMovies.flatMap { it.toGenreCrossRefs() },
            )
            favoritesDao.replaceFavorites(
                remoteMovies.map { movie ->
                    FavoriteEntity(
                        movieId = movie.imdbId,
                        createdAt = currentTimestamp(),
                    )
                },
            )
        }
    }

    override suspend fun toggleFavorite(movieId: String) {
        val current = favoritesDao.getFavorite(movieId)
        if (current == null) {
            addFavoriteOptimistically(movieId)
        } else {
            removeFavorite(movieId)
        }
    }

    override suspend fun removeFavorite(movieId: String) {
        val previous = favoritesDao.getFavorite(movieId)
        favoritesDao.deleteFavorite(movieId)

        runCatching {
            favoritesApi.removeFavorite(movieId)
        }.onFailure { error ->
            if (error is CancellationException) throw error
            previous?.let { favoritesDao.upsertFavorite(it) }
            throw RepositoryException("Failed to remove favorite.", error)
        }
    }

    override suspend fun clearFavoritesLocal() {
        favoritesDao.clearFavorites()
    }

    private suspend fun addFavoriteOptimistically(movieId: String) {
        val optimistic = FavoriteEntity(
            movieId = movieId,
            createdAt = currentTimestamp(),
        )
        favoritesDao.upsertFavorite(optimistic)

        runCatching {
            favoritesApi.addFavorite(movieId)
        }.onFailure { error ->
            if (error is CancellationException) throw error
            favoritesDao.deleteFavorite(movieId)
            throw RepositoryException("Failed to add favorite.", error)
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
