package rs.raf.showtime.movies.domain

data class Movie(
    val movieId: String,
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val imdbRating: Double? = null,
    val imdbVotes: Int? = null,
    val tmdbRating: Double? = null,
    val tmdbVotes: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val genres: List<Genre> = emptyList(),
)

data class MovieDetails(
    val movie: Movie,
    val tagline: String? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val languageCode: String? = null,
    val homepage: String? = null,
    val cast: List<CastMember> = emptyList(),
    val images: List<MovieImage> = emptyList(),
    val videos: List<MovieVideo> = emptyList(),
)

data class Genre(
    val id: Int,
    val name: String,
)

data class CastMember(
    val personId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    val profilePath: String? = null,
    val orderIndex: Int? = null,
)

data class MovieImage(
    val type: String,
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    val voteAverage: Double? = null,
    val language: String? = null,
)

data class MovieVideo(
    val key: String,
    val site: String,
    val name: String? = null,
    val type: String? = null,
    val official: Boolean = false,
    val publishedAt: String? = null,
)

data class MovieFilters(
    val query: String = "",
    val genreId: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Double? = null,
    val sortOption: MovieSortOption = MovieSortOption.Rating,
    val sortOrder: SortOrder = SortOrder.Descending,
)

enum class MovieSortOption(val apiValue: String, val localValue: String) {
    Rating(apiValue = "imdb_rating", localValue = "rating"),
    Year(apiValue = "year", localValue = "year"),
    Title(apiValue = "title", localValue = "title"),
}

enum class SortOrder(val apiValue: String, val localValue: String) {
    Ascending(apiValue = "asc", localValue = "asc"),
    Descending(apiValue = "desc", localValue = "desc"),
}
