package rs.raf.showtime.movies.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "movie_images",
    primaryKeys = ["movieId", "type", "filePath"],
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
data class MovieImageEntity(
    val movieId: String,
    val type: String,
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    val voteAverage: Double? = null,
    val language: String? = null,
)
