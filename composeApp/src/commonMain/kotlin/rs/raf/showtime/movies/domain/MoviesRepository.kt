package rs.raf.showtime.movies.domain

import kotlinx.coroutines.flow.Flow

interface MoviesRepository {
    fun observeMovies(filters: MovieFilters): Flow<List<Movie>>
    fun observeMovieDetails(movieId: String): Flow<MovieDetails?>
    fun observeGenres(): Flow<List<Genre>>

    suspend fun refreshMovies(filters: MovieFilters, page: Int, pageSize: Int)
    suspend fun refreshMovieDetails(movieId: String)
    suspend fun refreshGenres()
    suspend fun bootstrapQuizPoolIfNeeded(limit: Int = 100)
    suspend fun countMoviesWithImages(): Int
}
