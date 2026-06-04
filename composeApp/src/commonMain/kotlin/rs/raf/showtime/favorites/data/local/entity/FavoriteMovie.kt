package rs.raf.showtime.favorites.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import rs.raf.showtime.movies.data.local.entity.MovieEntity

data class FavoriteMovie(
    @Embedded val favorite: FavoriteEntity,
    @Relation(parentColumn = "movieId", entityColumn = "movieId")
    val movie: MovieEntity?,
)
