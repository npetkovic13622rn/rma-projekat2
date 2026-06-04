package rs.raf.showtime.movies.data.mapper

import rs.raf.showtime.movies.data.local.entity.CastMemberEntity
import rs.raf.showtime.movies.data.local.entity.GenreEntity
import rs.raf.showtime.movies.data.local.entity.MovieDetailsEntity
import rs.raf.showtime.movies.data.local.entity.MovieDetailsWithCastAndImages
import rs.raf.showtime.movies.data.local.entity.MovieEntity
import rs.raf.showtime.movies.data.local.entity.MovieGenreCrossRefEntity
import rs.raf.showtime.movies.data.local.entity.MovieImageEntity
import rs.raf.showtime.movies.data.local.entity.MovieVideoEntity
import rs.raf.showtime.movies.data.local.entity.MovieWithGenres
import rs.raf.showtime.movies.data.remote.dto.CastMemberDto
import rs.raf.showtime.movies.data.remote.dto.GenreDto
import rs.raf.showtime.movies.data.remote.dto.MovieDetailsDto
import rs.raf.showtime.movies.data.remote.dto.MovieImageDto
import rs.raf.showtime.movies.data.remote.dto.MovieListItemDto
import rs.raf.showtime.movies.data.remote.dto.MovieVideoDto
import rs.raf.showtime.movies.domain.CastMember
import rs.raf.showtime.movies.domain.Genre
import rs.raf.showtime.movies.domain.Movie
import rs.raf.showtime.movies.domain.MovieDetails
import rs.raf.showtime.movies.domain.MovieImage
import rs.raf.showtime.movies.domain.MovieVideo

fun MovieListItemDto.toMovieEntity(updatedAt: Long? = null): MovieEntity =
    MovieEntity(
        movieId = imdbId,
        title = title,
        year = year,
        runtime = runtime,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        posterPath = posterPath,
        updatedAt = updatedAt,
    )

fun MovieDetailsDto.toMovieEntity(updatedAt: Long? = null): MovieEntity =
    MovieEntity(
        movieId = imdbId,
        tmdbId = tmdbId,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        releaseDate = releaseDate,
        year = year,
        runtime = runtime,
        popularity = popularity,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = tmdbRating,
        tmdbVotes = tmdbVotes,
        posterPath = posterPath,
        backdropPath = backdropPath,
        updatedAt = updatedAt,
    )

fun MovieDetailsDto.toMovieDetailsEntity(syncedAt: Long? = null): MovieDetailsEntity =
    MovieDetailsEntity(
        movieId = imdbId,
        tagline = tagline,
        budget = budget,
        revenue = revenue,
        languageCode = languageCode,
        homepage = homepage,
        syncedAt = syncedAt,
    )

fun GenreDto.toGenreEntity(): GenreEntity =
    GenreEntity(
        genreId = id,
        name = name,
    )

fun MovieListItemDto.toGenreCrossRefs(): List<MovieGenreCrossRefEntity> =
    genres.map { genre ->
        MovieGenreCrossRefEntity(movieId = imdbId, genreId = genre.id)
    }

fun MovieDetailsDto.toGenreCrossRefs(): List<MovieGenreCrossRefEntity> =
    genres.map { genre ->
        MovieGenreCrossRefEntity(movieId = imdbId, genreId = genre.id)
    }

fun CastMemberDto.toCastMemberEntity(
    movieId: String,
    orderIndex: Int? = null,
): CastMemberEntity =
    CastMemberEntity(
        movieId = movieId,
        personId = imdbId,
        name = name,
        professions = professions,
        department = department,
        profilePath = profilePath,
        orderIndex = orderIndex,
    )

fun MovieImageDto.toMovieImageEntity(
    movieId: String,
    type: String,
): MovieImageEntity =
    MovieImageEntity(
        movieId = movieId,
        type = type,
        filePath = filePath,
        width = width,
        height = height,
        voteAverage = voteAverage,
        language = language,
    )

fun MovieVideoDto.toMovieVideoEntity(movieId: String): MovieVideoEntity =
    MovieVideoEntity(
        movieId = movieId,
        key = key,
        site = site,
        name = name,
        type = type,
        official = official,
        publishedAt = publishedAt,
    )

fun GenreEntity.toDomain(): Genre =
    Genre(
        id = genreId,
        name = name,
    )

fun MovieEntity.toDomain(genres: List<Genre> = emptyList()): Movie =
    Movie(
        movieId = movieId,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        releaseDate = releaseDate,
        year = year,
        runtime = runtime,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = tmdbRating,
        tmdbVotes = tmdbVotes,
        posterPath = posterPath,
        backdropPath = backdropPath,
        genres = genres,
    )

fun MovieWithGenres.toDomain(): Movie =
    movie.toDomain(genres = genres.map { it.toDomain() })

fun CastMemberEntity.toDomain(): CastMember =
    CastMember(
        personId = personId,
        name = name,
        professions = professions,
        department = department,
        profilePath = profilePath,
        orderIndex = orderIndex,
    )

fun MovieImageEntity.toDomain(): MovieImage =
    MovieImage(
        type = type,
        filePath = filePath,
        width = width,
        height = height,
        voteAverage = voteAverage,
        language = language,
    )

fun MovieVideoEntity.toDomain(): MovieVideo =
    MovieVideo(
        key = key,
        site = site,
        name = name,
        type = type,
        official = official,
        publishedAt = publishedAt,
    )

fun MovieDetailsWithCastAndImages.toDomain(): MovieDetails? {
    val movie = movie ?: return null
    return MovieDetails(
        movie = movie.toDomain(genres = genres.map { it.toDomain() }),
        tagline = details.tagline,
        budget = details.budget,
        revenue = details.revenue,
        languageCode = details.languageCode,
        homepage = details.homepage,
        cast = cast
            .sortedBy { it.orderIndex ?: Int.MAX_VALUE }
            .map { it.toDomain() },
        images = images.map { it.toDomain() },
        videos = videos.map { it.toDomain() },
    )
}
