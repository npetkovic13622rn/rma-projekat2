package rs.raf.showtime.favorites.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.raf.showtime.favorites.data.local.entity.FavoriteEntity
import rs.raf.showtime.favorites.data.local.entity.FavoriteMovie
import rs.raf.showtime.movies.data.local.entity.MovieWithGenres

@Dao
interface FavoritesDao {

    @Transaction
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<FavoriteMovie>>

    @Query("SELECT movieId FROM favorites ORDER BY createdAt DESC")
    fun observeFavoriteMovieIds(): Flow<List<String>>

    @Transaction
    @Query(
        """
        SELECT movies.* FROM movies
        INNER JOIN favorites ON movies.movieId = favorites.movieId
        ORDER BY favorites.createdAt DESC
        """,
    )
    fun observeFavoriteMovies(): Flow<List<MovieWithGenres>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId = :movieId)")
    fun isFavorite(movieId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE movieId = :movieId")
    suspend fun getFavorite(movieId: String): FavoriteEntity?

    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun deleteFavorite(movieId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()

    @Upsert
    suspend fun upsertFavorites(favorites: List<FavoriteEntity>)

    @Transaction
    suspend fun replaceFavorites(favorites: List<FavoriteEntity>) {
        clearFavorites()
        upsertFavorites(favorites)
    }
}
