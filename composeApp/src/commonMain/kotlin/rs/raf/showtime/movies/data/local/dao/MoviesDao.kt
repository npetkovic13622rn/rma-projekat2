package rs.raf.showtime.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.raf.showtime.movies.data.local.entity.CastMemberEntity
import rs.raf.showtime.movies.data.local.entity.GenreEntity
import rs.raf.showtime.movies.data.local.entity.MovieDetailsEntity
import rs.raf.showtime.movies.data.local.entity.MovieDetailsWithCastAndImages
import rs.raf.showtime.movies.data.local.entity.MovieEntity
import rs.raf.showtime.movies.data.local.entity.MovieGenreCrossRefEntity
import rs.raf.showtime.movies.data.local.entity.MovieImageEntity
import rs.raf.showtime.movies.data.local.entity.MovieVideoEntity
import rs.raf.showtime.movies.data.local.entity.MovieWithGenres

@Dao
interface MoviesDao {

    @Transaction
    @Query(
        """
        SELECT * FROM movies
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%')
            AND (:genreId IS NULL OR movieId IN (
                SELECT movieId FROM movie_genre_cross_refs WHERE genreId = :genreId
            ))
            AND (:minYear IS NULL OR year >= :minYear)
            AND (:maxYear IS NULL OR year <= :maxYear)
            AND (:minRating IS NULL OR imdbRating >= :minRating)
        ORDER BY
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'asc' THEN title END ASC,
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'desc' THEN title END DESC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'asc' THEN year END ASC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'desc' THEN year END DESC,
            CASE WHEN :sortBy = 'rating' AND :sortOrder = 'asc' THEN imdbRating END ASC,
            CASE WHEN :sortBy = 'rating' AND :sortOrder = 'desc' THEN imdbRating END DESC,
            title ASC
        """
    )
    fun observeMovies(
        query: String? = null,
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Double? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
    ): Flow<List<MovieWithGenres>>

    @Transaction
    @Query("SELECT * FROM movies WHERE movieId = :movieId")
    fun observeMovieById(movieId: String): Flow<MovieWithGenres?>

    @Transaction
    @Query("SELECT * FROM movie_details WHERE movieId = :movieId")
    fun observeMovieDetails(movieId: String): Flow<MovieDetailsWithCastAndImages?>

    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun observeGenres(): Flow<List<GenreEntity>>

    @Query(
        """
        SELECT COUNT(DISTINCT movies.movieId)
        FROM movies
        LEFT JOIN movie_images ON movies.movieId = movie_images.movieId
            AND movie_images.type != 'logo'
        WHERE (movies.posterPath IS NOT NULL AND movies.posterPath != '')
            OR (movies.backdropPath IS NOT NULL AND movies.backdropPath != '')
            OR movie_images.filePath IS NOT NULL
        """,
    )
    suspend fun countQuizCandidateMovies(): Int

    @Query(
        """
        SELECT DISTINCT movies.* FROM movies
        LEFT JOIN movie_images ON movies.movieId = movie_images.movieId
            AND movie_images.type != 'logo'
        WHERE (movies.posterPath IS NOT NULL AND movies.posterPath != '')
            OR (movies.backdropPath IS NOT NULL AND movies.backdropPath != '')
            OR movie_images.filePath IS NOT NULL
        """,
    )
    suspend fun getQuizCandidateMovies(): List<MovieEntity>

    @Query("SELECT * FROM movie_images WHERE movieId = :movieId AND type != 'logo'")
    suspend fun getQuizImagesForMovie(movieId: String): List<MovieImageEntity>

    @Query(
        """
        SELECT * FROM cast_members
        WHERE movieId = :movieId
        ORDER BY
            CASE WHEN orderIndex IS NULL THEN 1 ELSE 0 END ASC,
            orderIndex ASC,
            name ASC
        """,
    )
    suspend fun getQuizCastForMovie(movieId: String): List<CastMemberEntity>

    @Query("SELECT * FROM cast_members ORDER BY name ASC")
    suspend fun getAllQuizCastMembers(): List<CastMemberEntity>

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Upsert
    suspend fun upsertMovieDetails(details: MovieDetailsEntity)

    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Upsert
    suspend fun upsertMovieGenreCrossRefs(crossRefs: List<MovieGenreCrossRefEntity>)

    @Upsert
    suspend fun upsertCast(cast: List<CastMemberEntity>)

    @Upsert
    suspend fun upsertImages(images: List<MovieImageEntity>)

    @Upsert
    suspend fun upsertVideos(videos: List<MovieVideoEntity>)

    @Query("DELETE FROM movie_genre_cross_refs WHERE movieId = :movieId")
    suspend fun deleteGenreLinksForMovie(movieId: String)

    @Query("DELETE FROM cast_members WHERE movieId = :movieId")
    suspend fun deleteCastForMovie(movieId: String)

    @Query("DELETE FROM movie_images WHERE movieId = :movieId")
    suspend fun deleteImagesForMovie(movieId: String)

    @Query("DELETE FROM movie_videos WHERE movieId = :movieId")
    suspend fun deleteVideosForMovie(movieId: String)

    @Query("DELETE FROM movies")
    suspend fun clearMovieCatalog()

    @Query(
        """
        SELECT COUNT(DISTINCT movies.movieId)
        FROM movies
        LEFT JOIN movie_images ON movies.movieId = movie_images.movieId
        WHERE movies.posterPath IS NOT NULL
            OR movies.backdropPath IS NOT NULL
            OR movie_images.filePath IS NOT NULL
        """
    )
    suspend fun countMoviesWithImages(): Int

    @Transaction
    suspend fun replaceMovieGenreLinks(movieId: String, genreIds: List<Int>) {
        deleteGenreLinksForMovie(movieId)
        upsertMovieGenreCrossRefs(
            genreIds.map { genreId ->
                MovieGenreCrossRefEntity(movieId = movieId, genreId = genreId)
            },
        )
    }

    @Transaction
    suspend fun refreshMoviesTransaction(
        movies: List<MovieEntity>,
        genres: List<GenreEntity>,
        crossRefs: List<MovieGenreCrossRefEntity>,
    ) {
        upsertMovies(movies)
        upsertGenres(genres)
        crossRefs.map { it.movieId }.distinct().forEach { movieId ->
            deleteGenreLinksForMovie(movieId)
        }
        upsertMovieGenreCrossRefs(crossRefs)
    }

    @Transaction
    suspend fun refreshMovieDetailsTransaction(
        movie: MovieEntity,
        details: MovieDetailsEntity,
        genres: List<GenreEntity>,
        genreIds: List<Int>,
        cast: List<CastMemberEntity>,
        images: List<MovieImageEntity>,
        videos: List<MovieVideoEntity>,
    ) {
        upsertMovies(listOf(movie))
        upsertMovieDetails(details)
        upsertGenres(genres)
        replaceMovieGenreLinks(movie.movieId, genreIds)
        deleteCastForMovie(movie.movieId)
        upsertCast(cast)
        deleteImagesForMovie(movie.movieId)
        upsertImages(images)
        deleteVideosForMovie(movie.movieId)
        upsertVideos(videos)
    }
}
