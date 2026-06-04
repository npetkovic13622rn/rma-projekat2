package rs.raf.showtime.watchlist.data

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import rs.raf.showtime.movies.data.remote.dto.MovieListItemDto

interface WatchlistApi {

    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieListItemDto>

    @POST("me/watchlist/{movie_id}")
    suspend fun addToWatchlist(
        @Path("movie_id") movieId: String,
    )

    @DELETE("me/watchlist/{movie_id}")
    suspend fun removeFromWatchlist(
        @Path("movie_id") movieId: String,
    )
}
