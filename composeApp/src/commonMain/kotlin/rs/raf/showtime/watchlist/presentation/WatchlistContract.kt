package rs.raf.showtime.watchlist.presentation

import rs.raf.showtime.movies.domain.Movie

interface WatchlistContract {

    data class ViewState(
        val movies: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null,
        val emptyMessage: String? = null,
    )

    sealed interface Intent {
        data object ScreenStarted : Intent
        data object Refresh : Intent
        data class MovieClicked(val movieId: String) : Intent
        data class RemoveClicked(val movieId: String) : Intent
        data object ErrorShown : Intent
    }

    sealed interface Effect {
        data class NavigateToMovieDetails(val movieId: String) : Effect
        data class ShowMessage(val message: String) : Effect
    }
}
