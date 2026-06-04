package rs.raf.showtime.watchlist.domain

import kotlinx.coroutines.flow.Flow
import rs.raf.showtime.movies.domain.Movie

interface WatchlistRepository {
    fun observeWatchlistMovies(): Flow<List<Movie>>
    fun observeWatchlistMovieIds(): Flow<Set<String>>
    fun observeIsInWatchlist(movieId: String): Flow<Boolean>
    suspend fun syncWatchlist()
    suspend fun toggleWatchlist(movieId: String)
    suspend fun removeFromWatchlist(movieId: String)
    suspend fun clearWatchlistLocal()
}
