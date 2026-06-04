package rs.raf.showtime.movies.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.showtime.core.util.RepositoryException
import rs.raf.showtime.favorites.domain.FavoritesRepository
import rs.raf.showtime.movies.domain.MoviesRepository
import rs.raf.showtime.watchlist.domain.WatchlistRepository

class MovieDetailsViewModel(
    private val moviesRepository: MoviesRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MovieDetailsContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MovieDetailsContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    private var detailsJob: Job? = null
    private var favoriteJob: Job? = null
    private var watchlistJob: Job? = null

    fun setIntent(intent: MovieDetailsContract.Intent) {
        when (intent) {
            is MovieDetailsContract.Intent.ScreenStarted -> onScreenStarted(intent.movieId)
            MovieDetailsContract.Intent.Refresh -> refresh()
            MovieDetailsContract.Intent.FavoriteClicked -> toggleFavorite()
            MovieDetailsContract.Intent.WatchlistClicked -> toggleWatchlist()
            MovieDetailsContract.Intent.BackClicked ->
                _effects.tryEmit(MovieDetailsContract.Effect.NavigateBack)
            MovieDetailsContract.Intent.ErrorShown ->
                setState { copy(errorMessage = null) }
        }
    }

    private fun onScreenStarted(movieId: String) {
        if (_state.value.movieId == movieId && detailsJob != null) return
        setState {
            MovieDetailsContract.ViewState(
                movieId = movieId,
                isLoading = true,
            )
        }
        observeMovieDetails(movieId)
        observeFavorite(movieId)
        observeWatchlist(movieId)
        refresh()
    }

    private fun observeMovieDetails(movieId: String) {
        detailsJob?.cancel()
        detailsJob = viewModelScope.launch {
            moviesRepository.observeMovieDetails(movieId).collect { details ->
                setState {
                    copy(
                        movie = details,
                        isLoading = isLoading && details == null,
                    )
                }
            }
        }
    }

    private fun observeFavorite(movieId: String) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            favoritesRepository.observeIsFavorite(movieId).collect { isFavorite ->
                setState { copy(isFavorite = isFavorite) }
            }
        }
    }

    private fun observeWatchlist(movieId: String) {
        watchlistJob?.cancel()
        watchlistJob = viewModelScope.launch {
            watchlistRepository.observeIsInWatchlist(movieId).collect { isInWatchlist ->
                setState { copy(isInWatchlist = isInWatchlist) }
            }
        }
    }

    private fun refresh() {
        val movieId = _state.value.movieId
        if (movieId.isBlank()) return

        viewModelScope.launch {
            setState {
                copy(
                    isRefreshing = movie != null,
                    isLoading = movie == null,
                    errorMessage = null,
                )
            }

            runCatching {
                moviesRepository.refreshMovieDetails(movieId)
            }.onFailure { error ->
                if (error is CancellationException) throw error
                val message = error.asUserMessage()
                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = message,
                    )
                }
                _effects.tryEmit(MovieDetailsContract.Effect.ShowMessage(message))
            }.onSuccess {
                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            }
        }
    }

    private fun toggleFavorite() {
        val movieId = _state.value.movieId
        if (movieId.isBlank()) return

        viewModelScope.launch {
            runCatching {
                favoritesRepository.toggleFavorite(movieId)
            }.onFailure { error ->
                if (error is CancellationException) throw error
                val message = error.asFavoriteMessage()
                _effects.tryEmit(MovieDetailsContract.Effect.ShowMessage(message))
            }
        }
    }

    private fun toggleWatchlist() {
        val movieId = _state.value.movieId
        if (movieId.isBlank()) return

        viewModelScope.launch {
            runCatching {
                watchlistRepository.toggleWatchlist(movieId)
            }.onFailure { error ->
                if (error is CancellationException) throw error
                val message = error.asWatchlistMessage()
                _effects.tryEmit(MovieDetailsContract.Effect.ShowMessage(message))
            }
        }
    }

    private fun Throwable.asUserMessage(): String =
        when (this) {
            is RepositoryException -> message ?: "Movie details refresh failed."
            else -> message ?: "Movie details refresh failed."
        }

    private fun Throwable.asFavoriteMessage(): String =
        when (this) {
            is RepositoryException -> message ?: "Favorite update failed."
            else -> message ?: "Favorite update failed."
        }

    private fun Throwable.asWatchlistMessage(): String =
        when (this) {
            is RepositoryException -> message ?: "Watchlist update failed."
            else -> message ?: "Watchlist update failed."
        }

    private fun setState(reducer: MovieDetailsContract.ViewState.() -> MovieDetailsContract.ViewState) {
        _state.getAndUpdate(reducer)
    }
}
