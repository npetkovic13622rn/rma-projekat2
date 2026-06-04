package rs.raf.showtime.favorites.data

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import rs.raf.showtime.movies.data.remote.dto.MovieListItemDto

interface FavoritesApi {

    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieListItemDto>

    @POST("me/favorites/{movie_id}")
    suspend fun addFavorite(
        @Path("movie_id") movieId: String,
    )

    @DELETE("me/favorites/{movie_id}")
    suspend fun removeFavorite(
        @Path("movie_id") movieId: String,
    )
}
