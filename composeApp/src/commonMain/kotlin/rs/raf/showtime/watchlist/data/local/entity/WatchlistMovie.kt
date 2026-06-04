package rs.raf.showtime.watchlist.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import rs.raf.showtime.movies.data.local.entity.MovieEntity

data class WatchlistMovie(
    @Embedded val watchlist: WatchlistEntity,
    @Relation(parentColumn = "movieId", entityColumn = "movieId")
    val movie: MovieEntity?,
)
