package rs.raf.showtime.movies.presentation.details

import rs.raf.showtime.movies.domain.MovieDetails

interface MovieDetailsContract {

    data class ViewState(
        val movieId: String = "",
        val movie: MovieDetails? = null,
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null,
        val isFavorite: Boolean = false,
        val isInWatchlist: Boolean = false,
    )

    sealed interface Intent {
        data class ScreenStarted(val movieId: String) : Intent
        data object Refresh : Intent
        data object FavoriteClicked : Intent
        data object WatchlistClicked : Intent
        data object BackClicked : Intent
        data object ErrorShown : Intent
    }

    sealed interface Effect {
        data object NavigateBack : Effect
        data class ShowMessage(val message: String) : Effect
    }
}
