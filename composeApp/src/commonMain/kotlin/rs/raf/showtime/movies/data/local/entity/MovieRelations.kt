package rs.raf.showtime.movies.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MovieWithGenres(
    @Embedded val movie: MovieEntity,
    @Relation(
        parentColumn = "movieId",
        entityColumn = "genreId",
        associateBy = Junction(
            value = MovieGenreCrossRefEntity::class,
            parentColumn = "movieId",
            entityColumn = "genreId",
        ),
    )
    val genres: List<GenreEntity>,
)

data class MovieDetailsWithCastAndImages(
    @Embedded val details: MovieDetailsEntity,
    @Relation(parentColumn = "movieId", entityColumn = "movieId")
    val movie: MovieEntity?,
    @Relation(
        parentColumn = "movieId",
        entityColumn = "genreId",
        associateBy = Junction(
            value = MovieGenreCrossRefEntity::class,
            parentColumn = "movieId",
            entityColumn = "genreId",
        ),
    )
    val genres: List<GenreEntity>,
    @Relation(parentColumn = "movieId", entityColumn = "movieId")
    val cast: List<CastMemberEntity>,
    @Relation(parentColumn = "movieId", entityColumn = "movieId")
    val images: List<MovieImageEntity>,
    @Relation(parentColumn = "movieId", entityColumn = "movieId")
    val videos: List<MovieVideoEntity>,
)
