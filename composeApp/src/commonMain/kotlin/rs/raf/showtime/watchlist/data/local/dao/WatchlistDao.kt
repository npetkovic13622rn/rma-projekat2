package rs.raf.showtime.watchlist.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.raf.showtime.watchlist.data.local.entity.WatchlistEntity
import rs.raf.showtime.watchlist.data.local.entity.WatchlistMovie
import rs.raf.showtime.movies.data.local.entity.MovieWithGenres

@Dao
interface WatchlistDao {

    @Transaction
    @Query("SELECT * FROM watchlist ORDER BY createdAt DESC")
    fun observeWatchlist(): Flow<List<WatchlistMovie>>

    @Query("SELECT movieId FROM watchlist ORDER BY createdAt DESC")
    fun observeWatchlistMovieIds(): Flow<List<String>>

    @Transaction
    @Query(
        """
        SELECT movies.* FROM movies
        INNER JOIN watchlist ON movies.movieId = watchlist.movieId
        ORDER BY watchlist.createdAt DESC
        """,
    )
    fun observeWatchlistMovies(): Flow<List<MovieWithGenres>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE movieId = :movieId)")
    fun isInWatchlist(movieId: String): Flow<Boolean>

    @Query("SELECT * FROM watchlist WHERE movieId = :movieId")
    suspend fun getWatchlist(movieId: String): WatchlistEntity?

    @Upsert
    suspend fun upsertWatchlist(watchlist: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun deleteWatchlist(movieId: String)

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()

    @Upsert
    suspend fun upsertWatchlistItems(watchlist: List<WatchlistEntity>)

    @Transaction
    suspend fun replaceWatchlist(watchlist: List<WatchlistEntity>) {
        clearWatchlist()
        upsertWatchlistItems(watchlist)
    }
}
