package rs.raf.showtime.movies.presentation.list

import rs.raf.showtime.movies.domain.Genre
import rs.raf.showtime.movies.domain.Movie
import rs.raf.showtime.movies.domain.MovieFilters
import rs.raf.showtime.movies.domain.MovieSortOption
import rs.raf.showtime.movies.domain.SortOrder

interface MoviesListContract {

    data class ViewState(
        val movies: List<Movie> = emptyList(),
        val genres: List<Genre> = emptyList(),
        val filters: MovieFilters = MovieFilters(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isLoadingNextPage: Boolean = false,
        val isOffline: Boolean = false,
        val errorMessage: String? = null,
        val emptyMessage: String? = null,
        val currentPage: Int = 1,
        val canLoadMore: Boolean = true,
    )

    sealed interface Intent {
        data object ScreenStarted : Intent
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class QueryChanged(val query: String) : Intent
        data object SearchSubmitted : Intent
        data class GenreSelected(val genreId: Int?) : Intent
        data class YearRangeChanged(val minYear: Int?, val maxYear: Int?) : Intent
        data class MinRatingChanged(val minRating: Double?) : Intent
        data class SortChanged(
            val sortOption: MovieSortOption,
            val sortOrder: SortOrder,
        ) : Intent
        data object ClearFilters : Intent
        data class MovieClicked(val movieId: String) : Intent
        data object ErrorShown : Intent
    }

    sealed interface Effect {
        data class NavigateToMovieDetails(val movieId: String) : Effect
        data class ShowMessage(val message: String) : Effect
    }
}
