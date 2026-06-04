package rs.raf.showtime.movies.data.remote

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import rs.raf.showtime.movies.data.remote.dto.CastMemberDto
import rs.raf.showtime.movies.data.remote.dto.GenreDto
import rs.raf.showtime.movies.data.remote.dto.ImageConfigurationDto
import rs.raf.showtime.movies.data.remote.dto.MovieDetailsDto
import rs.raf.showtime.movies.data.remote.dto.MovieImagesDto
import rs.raf.showtime.movies.data.remote.dto.MovieListItemDto
import rs.raf.showtime.movies.data.remote.dto.MovieVideoDto
import rs.raf.showtime.movies.data.remote.dto.PaginatedResponseDto

interface MoviesApi {

    @GET("movies")
    suspend fun getMovies(
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("query") query: String? = null,
        @Query("genre_id") genreId: Int? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Double? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
    ): PaginatedResponseDto<MovieListItemDto>

    @GET("movies/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: String,
    ): MovieDetailsDto

    @GET("movies/{movie_id}/cast")
    suspend fun getMovieCast(
        @Path("movie_id") movieId: String,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
    ): PaginatedResponseDto<CastMemberDto>

    @GET("movies/{movie_id}/images")
    suspend fun getMovieImages(
        @Path("movie_id") movieId: String,
        @Query("type") type: String? = null,
    ): MovieImagesDto

    @GET("movies/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: String,
        @Query("type") type: String? = null,
    ): List<MovieVideoDto>

    @GET("genres")
    suspend fun getGenres(): List<GenreDto>

    @GET("config")
    suspend fun getImageConfiguration(): ImageConfigurationDto

    @GET("actors/search")
    suspend fun searchActors(
        @Query("query") query: String,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
    ): PaginatedResponseDto<CastMemberDto>
}
