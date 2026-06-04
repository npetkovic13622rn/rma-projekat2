package rs.raf.showtime.watchlist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import rs.raf.showtime.movies.data.local.entity.MovieEntity

@Entity(
    tableName = "watchlist",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("movieId")],
)
data class WatchlistEntity(
    @PrimaryKey val movieId: String,
    val createdAt: Long,
)
