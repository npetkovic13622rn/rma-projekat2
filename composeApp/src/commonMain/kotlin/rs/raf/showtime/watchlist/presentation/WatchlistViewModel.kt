package rs.raf.showtime.watchlist.presentation

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
import rs.raf.showtime.watchlist.domain.WatchlistRepository

class WatchlistViewModel(
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<WatchlistContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    private var hasStarted = false
    private var watchlistJob: Job? = null

    fun setIntent(intent: WatchlistContract.Intent) {
        when (intent) {
            WatchlistContract.Intent.ScreenStarted -> onScreenStarted()
            WatchlistContract.Intent.Refresh -> syncWatchlist()
            is WatchlistContract.Intent.MovieClicked ->
                _effects.tryEmit(WatchlistContract.Effect.NavigateToMovieDetails(intent.movieId))
            is WatchlistContract.Intent.RemoveClicked -> removeFromWatchlist(intent.movieId)
            WatchlistContract.Intent.ErrorShown -> setState { copy(errorMessage = null) }
        }
    }

    private fun onScreenStarted() {
        if (hasStarted) return
        hasStarted = true
        observeWatchlist()
        syncWatchlist(showInitialLoading = true)
    }

    private fun observeWatchlist() {
        watchlistJob?.cancel()
        watchlistJob = viewModelScope.launch {
            watchlistRepository.observeWatchlistMovies().collect { movies ->
                setState {
                    copy(
                        movies = movies,
                        emptyMessage = when {
                            isLoading || isRefreshing -> null
                            movies.isEmpty() -> "No movies in your watchlist yet."
                            else -> null
                        },
                    )
                }
            }
        }
    }

    private fun syncWatchlist(showInitialLoading: Boolean = false) {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = showInitialLoading && movies.isEmpty(),
                    isRefreshing = !showInitialLoading || movies.isNotEmpty(),
                    errorMessage = null,
                )
            }
            runCatching {
                watchlistRepository.syncWatchlist()
            }.onFailure { error ->
                if (error is CancellationException) throw error
                val message = error.asUserMessage("Failed to sync watchlist.")
                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = message,
                        emptyMessage = if (movies.isEmpty()) "Watchlist is unavailable offline." else null,
                    )
                }
                _effects.tryEmit(WatchlistContract.Effect.ShowMessage(message))
            }.onSuccess {
                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        emptyMessage = if (movies.isEmpty()) "No movies in your watchlist yet." else null,
                    )
                }
            }
        }
    }

    private fun removeFromWatchlist(movieId: String) {
        viewModelScope.launch {
            runCatching {
                watchlistRepository.removeFromWatchlist(movieId)
            }.onFailure { error ->
                if (error is CancellationException) throw error
                val message = error.asUserMessage("Failed to remove from watchlist.")
                setState { copy(errorMessage = message) }
                _effects.tryEmit(WatchlistContract.Effect.ShowMessage(message))
            }
        }
    }

    private fun Throwable.asUserMessage(defaultMessage: String): String =
        when (this) {
            is RepositoryException -> message ?: defaultMessage
            else -> message ?: defaultMessage
        }

    private fun setState(reducer: WatchlistContract.ViewState.() -> WatchlistContract.ViewState) {
        _state.getAndUpdate(reducer)
    }
}
