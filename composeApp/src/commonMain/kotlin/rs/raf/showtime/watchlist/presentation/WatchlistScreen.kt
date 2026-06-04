package rs.raf.showtime.watchlist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import rs.raf.showtime.movies.domain.Movie
import rs.raf.showtime.movies.presentation.util.ImageUrlBuilder

@Composable
fun WatchlistRoute(
    onNavigateToMovieDetails: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WatchlistViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.setIntent(WatchlistContract.Intent.ScreenStarted)
        viewModel.effects.collect { effect ->
            when (effect) {
                is WatchlistContract.Effect.NavigateToMovieDetails ->
                    onNavigateToMovieDetails(effect.movieId)
                is WatchlistContract.Effect.ShowMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
            }
        }
    }

    WatchlistScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::setIntent,
    )
}

@Composable
fun WatchlistScreen(
    state: WatchlistContract.ViewState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onIntent: (WatchlistContract.Intent) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(onClick = onNavigateBack) {
                            Text("Movies")
                        }
                        Text(
                            text = "Watchlist",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            enabled = !state.isLoading && !state.isRefreshing,
                            onClick = { onIntent(WatchlistContract.Intent.Refresh) },
                        ) {
                            Text("Refresh")
                        }
                    }
                }
            }

            item {
                WatchlistStatus(state = state, onIntent = onIntent)
            }

            items(
                items = state.movies,
                key = { movie -> movie.movieId },
            ) { movie ->
                WatchlistMovieCard(
                    movie = movie,
                    onClick = {
                        onIntent(WatchlistContract.Intent.MovieClicked(movie.movieId))
                    },
                    onRemove = {
                        onIntent(WatchlistContract.Intent.RemoveClicked(movie.movieId))
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun WatchlistStatus(
    state: WatchlistContract.ViewState,
    onIntent: (WatchlistContract.Intent) -> Unit,
) {
    when {
        state.isLoading -> CenteredProgress()
        state.errorMessage != null -> ErrorMessage(
            message = state.errorMessage,
            onDismiss = { onIntent(WatchlistContract.Intent.ErrorShown) },
        )
        state.emptyMessage != null -> Text(
            text = state.emptyMessage,
            style = MaterialTheme.typography.bodyLarge,
        )
        state.isRefreshing -> Text(
            text = "Syncing watchlist...",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CenteredProgress() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            modifier = Modifier.clickable(onClick = onDismiss),
            text = "Dismiss",
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun WatchlistMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Poster(path = movie.posterPath)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = listOfNotNull(
                        movie.year?.toString(),
                        movie.imdbRating?.let { "IMDb ${it.toDisplayText()}" },
                    ).joinToString("  |  ").ifBlank { "Movie" },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}

@Composable
private fun Poster(path: String?) {
    val posterUrl = ImageUrlBuilder.posterUrl(path)

    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 96.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (posterUrl != null) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = "Poster",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Double.toDisplayText(): String {
    val rounded = (this * 10).toInt() / 10.0
    return rounded.toString()
}
