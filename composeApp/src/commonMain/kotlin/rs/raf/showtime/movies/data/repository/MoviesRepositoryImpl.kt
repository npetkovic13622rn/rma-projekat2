package rs.raf.showtime.movies.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import rs.raf.showtime.core.db.AppDatabase
import rs.raf.showtime.core.util.RepositoryException
import rs.raf.showtime.movies.data.mapper.toCastMemberEntity
import rs.raf.showtime.movies.data.mapper.toDomain
import rs.raf.showtime.movies.data.mapper.toGenreCrossRefs
import rs.raf.showtime.movies.data.mapper.toGenreEntity
import rs.raf.showtime.movies.data.mapper.toMovieDetailsEntity
import rs.raf.showtime.movies.data.mapper.toMovieEntity
import rs.raf.showtime.movies.data.mapper.toMovieImageEntity
import rs.raf.showtime.movies.data.mapper.toMovieVideoEntity
import rs.raf.showtime.movies.data.remote.MoviesApi
import rs.raf.showtime.movies.domain.Genre
import rs.raf.showtime.movies.domain.Movie
import rs.raf.showtime.movies.domain.MovieDetails
import rs.raf.showtime.movies.domain.MovieFilters
import rs.raf.showtime.movies.domain.MoviesRepository

class MoviesRepositoryImpl(
    appDatabase: AppDatabase,
    private val moviesApi: MoviesApi,
) : MoviesRepository {

    private val moviesDao = appDatabase.moviesDao()

    override fun observeMovies(filters: MovieFilters): Flow<List<Movie>> =
        moviesDao.observeMovies(
            query = filters.query.trim().takeIf { it.isNotEmpty() },
            genreId = filters.genreId,
            minYear = filters.minYear,
            maxYear = filters.maxYear,
            minRating = filters.minRating,
            sortBy = filters.sortOption.localValue,
            sortOrder = filters.sortOrder.localValue,
        )
            .distinctUntilChanged()
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeMovieDetails(movieId: String): Flow<MovieDetails?> =
        moviesDao.observeMovieDetails(movieId)
            .distinctUntilChanged()
            .map { row -> row?.toDomain() }

    override fun observeGenres(): Flow<List<Genre>> =
        moviesDao.observeGenres()
            .distinctUntilChanged()
            .map { genres -> genres.map { it.toDomain() } }

    override suspend fun refreshMovies(filters: MovieFilters, page: Int, pageSize: Int): Int {
        return runRepositoryCatching("Failed to refresh movies") {
            val response = moviesApi.getMovies(
                page = page,
                pageSize = pageSize,
                query = filters.query.trim().takeIf { it.isNotEmpty() },
                genreId = filters.genreId,
                minYear = filters.minYear,
                maxYear = filters.maxYear,
                minRating = filters.minRating,
                sortBy = filters.sortOption.apiValue,
                sortOrder = filters.sortOrder.apiValue,
            )
            val movies = response.items.map { it.toMovieEntity() }
            val genres = response.items
                .flatMap { it.genres }
                .distinctBy { it.id }
                .map { it.toGenreEntity() }
            val crossRefs = response.items.flatMap { it.toGenreCrossRefs() }

            moviesDao.refreshMoviesTransaction(
                movies = movies,
                genres = genres,
                crossRefs = crossRefs,
            )
            movies.size
        }
    }

    override suspend fun refreshMovieDetails(movieId: String) {
        runRepositoryCatching("Failed to refresh movie details") {
            val details = moviesApi.getMovieDetails(movieId)
            val cast = moviesApi.getMovieCast(
                movieId = movieId,
                page = 1,
                pageSize = 50,
            ).items
            val images = moviesApi.getMovieImages(movieId = movieId)
            val videos = runCatching {
                moviesApi.getMovieVideos(movieId = movieId)
            }.getOrElse { emptyList() }

            moviesDao.refreshMovieDetailsTransaction(
                movie = details.toMovieEntity(),
                details = details.toMovieDetailsEntity(),
                genres = details.genres.map { it.toGenreEntity() },
                genreIds = details.genres.map { it.id },
                cast = cast.mapIndexed { index, dto ->
                    dto.toCastMemberEntity(movieId = movieId, orderIndex = index)
                },
                images = images.posters.map { it.toMovieImageEntity(movieId, type = "poster") } +
                    images.backdrops.map { it.toMovieImageEntity(movieId, type = "backdrop") } +
                    images.logos.map { it.toMovieImageEntity(movieId, type = "logo") },
                videos = videos.map { it.toMovieVideoEntity(movieId) },
            )
        }
    }

    override suspend fun refreshGenres() {
        runRepositoryCatching("Failed to refresh genres") {
            moviesDao.upsertGenres(
                moviesApi.getGenres().map { it.toGenreEntity() },
            )
        }
    }

    override suspend fun bootstrapQuizPoolIfNeeded(limit: Int) {
        if (moviesDao.countMoviesWithImages() >= limit) return
        refreshMovies(
            filters = MovieFilters(),
            page = 1,
            pageSize = limit,
        )
    }

    override suspend fun countMoviesWithImages(): Int =
        moviesDao.countMoviesWithImages()

    private suspend fun <T> runRepositoryCatching(
        message: String,
        block: suspend () -> T,
    ): T {
        try {
            return block()
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            throw RepositoryException(message, error)
        }
    }
}
