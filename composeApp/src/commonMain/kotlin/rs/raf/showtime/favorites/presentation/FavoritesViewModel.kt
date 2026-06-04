package rs.raf.showtime.favorites.presentation

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

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<FavoritesContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    private var hasStarted = false
    private var favoritesJob: Job? = null

    fun setIntent(intent: FavoritesContract.Intent) {
        when (intent) {
            FavoritesContract.Intent.ScreenStarted -> onScreenStarted()
            FavoritesContract.Intent.Refresh -> syncFavorites()
            is FavoritesContract.Intent.MovieClicked ->
                _effects.tryEmit(FavoritesContract.Effect.NavigateToMovieDetails(intent.movieId))
            is FavoritesContract.Intent.RemoveClicked -> removeFavorite(intent.movieId)
            FavoritesContract.Intent.ErrorShown -> setState { copy(errorMessage = null) }
        }
    }

    private fun onScreenStarted() {
        if (hasStarted) return
        hasStarted = true
        observeFavorites()
        syncFavorites(showInitialLoading = true)
    }

    private fun observeFavorites() {
        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            favoritesRepository.observeFavoriteMovies().collect { movies ->
                setState {
                    copy(
                        movies = movies,
                        emptyMessage = when {
                            isLoading || isRefreshing -> null
                            movies.isEmpty() -> "No favorite movies yet."
                            else -> null
                        },
                    )
                }
            }
        }
    }

    private fun syncFavorites(showInitialLoading: Boolean = false) {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = showInitialLoading && movies.isEmpty(),
                    isRefreshing = !showInitialLoading || movies.isNotEmpty(),
                    errorMessage = null,
                )
            }
            runCatching {
                favoritesRepository.syncFavorites()
            }.onFailure { error ->
                if (error is CancellationException) throw error
                val message = error.asUserMessage("Failed to sync favorites.")
                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = message,
                        emptyMessage = if (movies.isEmpty()) "Favorites are unavailable offline." else null,
                    )
                }
                _effects.tryEmit(FavoritesContract.Effect.ShowMessage(message))
            }.onSuccess {
                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        emptyMessage = if (movies.isEmpty()) "No favorite movies yet." else null,
                    )
                }
            }
        }
    }

    private fun removeFavorite(movieId: String) {
        viewModelScope.launch {
            runCatching {
                favoritesRepository.removeFavorite(movieId)
            }.onFailure { error ->
                if (error is CancellationException) throw error
                val message = error.asUserMessage("Failed to remove favorite.")
                setState { copy(errorMessage = message) }
                _effects.tryEmit(FavoritesContract.Effect.ShowMessage(message))
            }
        }
    }

    private fun Throwable.asUserMessage(defaultMessage: String): String =
        when (this) {
            is RepositoryException -> message ?: defaultMessage
            else -> message ?: defaultMessage
        }

    private fun setState(reducer: FavoritesContract.ViewState.() -> FavoritesContract.ViewState) {
        _state.getAndUpdate(reducer)
    }
}
