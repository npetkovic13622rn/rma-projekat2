package rs.raf.showtime.movies.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.showtime.core.util.RepositoryException
import rs.raf.showtime.movies.domain.MovieFilters
import rs.raf.showtime.movies.domain.MoviesRepository

class MoviesListViewModel(
    private val moviesRepository: MoviesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MoviesListContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MoviesListContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    private var hasStarted = false
    private var moviesJob: Job? = null
    private var genresJob: Job? = null

    fun setIntent(intent: MoviesListContract.Intent) {
        when (intent) {
            MoviesListContract.Intent.ScreenStarted -> onScreenStarted()
            MoviesListContract.Intent.Refresh -> refreshFirstPage()
            MoviesListContract.Intent.LoadNextPage -> loadNextPage()
            is MoviesListContract.Intent.QueryChanged -> updateFilters(
                _state.value.filters.copy(query = intent.query),
                refresh = false,
            )
            MoviesListContract.Intent.SearchSubmitted -> refreshFirstPage()
            is MoviesListContract.Intent.GenreSelected -> updateFilters(
                _state.value.filters.copy(genreId = intent.genreId),
            )
            is MoviesListContract.Intent.YearRangeChanged -> updateFilters(
                _state.value.filters.copy(
                    minYear = intent.minYear,
                    maxYear = intent.maxYear,
                ),
            )
            is MoviesListContract.Intent.MinRatingChanged -> updateFilters(
                _state.value.filters.copy(minRating = intent.minRating),
            )
            is MoviesListContract.Intent.SortChanged -> updateFilters(
                _state.value.filters.copy(
                    sortOption = intent.sortOption,
                    sortOrder = intent.sortOrder,
                ),
            )
            MoviesListContract.Intent.ClearFilters -> updateFilters(MovieFilters())
            is MoviesListContract.Intent.MovieClicked -> {
                _effects.tryEmit(
                    MoviesListContract.Effect.NavigateToMovieDetails(intent.movieId),
                )
            }
            MoviesListContract.Intent.ErrorShown -> setState { copy(errorMessage = null) }
        }
    }

    private fun onScreenStarted() {
        if (hasStarted) return
        hasStarted = true
        observeMovies(_state.value.filters)
        observeGenres()
        refreshGenres()
        refreshFirstPage(showInitialLoading = true)
    }

    private fun observeMovies(filters: MovieFilters) {
        moviesJob?.cancel()
        moviesJob = viewModelScope.launch {
            moviesRepository.observeMovies(filters).collect { movies ->
                setState {
                    copy(
                        movies = movies,
                        emptyMessage = when {
                            isLoading || isRefreshing || isLoadingNextPage -> null
                            movies.isEmpty() -> "No movies match your filters."
                            else -> null
                        },
                    )
                }
            }
        }
    }

    private fun observeGenres() {
        if (genresJob != null) return
        genresJob = viewModelScope.launch {
            moviesRepository.observeGenres().collect { genres ->
                setState { copy(genres = genres) }
            }
        }
    }

    private fun updateFilters(
        filters: MovieFilters,
        refresh: Boolean = true,
    ) {
        setState {
            copy(
                filters = filters,
                currentPage = 1,
                canLoadMore = true,
                errorMessage = null,
                emptyMessage = null,
            )
        }
        observeMovies(filters)
        if (refresh) refreshFirstPage()
    }

    private fun refreshGenres() {
        viewModelScope.launch {
            runRepositoryAction {
                moviesRepository.refreshGenres()
            }
        }
    }

    private fun refreshFirstPage(showInitialLoading: Boolean = false) {
        viewModelScope.launch {
            setState {
                copy(
                    currentPage = 1,
                    isLoading = showInitialLoading,
                    isRefreshing = !showInitialLoading,
                    isOffline = false,
                    errorMessage = null,
                    emptyMessage = null,
                    canLoadMore = true,
                )
            }
            runRepositoryAction {
                moviesRepository.refreshMovies(
                    filters = _state.value.filters,
                    page = 1,
                    pageSize = PageSize,
                )
            }
            setState {
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    emptyMessage = if (movies.isEmpty()) "No movies available." else null,
                )
            }
        }
    }

    private fun loadNextPage() {
        val snapshot = _state.value
        if (!snapshot.canLoadMore || snapshot.isLoadingNextPage || snapshot.isLoading) return
        val nextPage = snapshot.currentPage + 1

        viewModelScope.launch {
            setState {
                copy(
                    currentPage = nextPage,
                    isLoadingNextPage = true,
                    errorMessage = null,
                )
            }
            val succeeded = runRepositoryAction {
                moviesRepository.refreshMovies(
                    filters = _state.value.filters,
                    page = nextPage,
                    pageSize = PageSize,
                )
            }
            setState {
                copy(
                    currentPage = if (succeeded) nextPage else snapshot.currentPage,
                    isLoadingNextPage = false,
                )
            }
        }
    }

    private suspend fun runRepositoryAction(action: suspend () -> Unit): Boolean {
        return runCatching { action() }
            .onFailure { error ->
                val message = error.asUserMessage()
                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingNextPage = false,
                        isOffline = true,
                        errorMessage = message,
                    )
                }
                _effects.tryEmit(MoviesListContract.Effect.ShowMessage(message))
            }
            .isSuccess
    }

    private fun Throwable.asUserMessage(): String =
        when (this) {
            is RepositoryException -> message ?: "Movie refresh failed."
            else -> message ?: "Movie refresh failed."
        }

    private fun setState(reducer: MoviesListContract.ViewState.() -> MoviesListContract.ViewState) {
        _state.getAndUpdate(reducer)
    }

    private companion object {
        const val PageSize = 30
    }
}
