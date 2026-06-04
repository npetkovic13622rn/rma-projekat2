package rs.raf.showtime.favorites.domain

import kotlinx.coroutines.flow.Flow
import rs.raf.showtime.movies.domain.Movie

interface FavoritesRepository {
    fun observeFavoriteMovies(): Flow<List<Movie>>
    fun observeFavoriteMovieIds(): Flow<Set<String>>
    fun observeIsFavorite(movieId: String): Flow<Boolean>
    suspend fun syncFavorites()
    suspend fun toggleFavorite(movieId: String)
    suspend fun removeFavorite(movieId: String)
    suspend fun clearFavoritesLocal()
}
