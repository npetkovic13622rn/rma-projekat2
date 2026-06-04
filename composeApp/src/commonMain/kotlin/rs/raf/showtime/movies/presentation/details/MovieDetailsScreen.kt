package rs.raf.showtime.movies.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import rs.raf.showtime.movies.domain.CastMember
import rs.raf.showtime.movies.domain.Movie
import rs.raf.showtime.movies.domain.MovieDetails
import rs.raf.showtime.movies.presentation.util.ImageUrlBuilder

@Composable
fun MovieDetailsRoute(
    movieId: String,
    onNavigateBack: () -> Unit,
    viewModel: MovieDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(movieId) {
        viewModel.setIntent(MovieDetailsContract.Intent.ScreenStarted(movieId))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MovieDetailsContract.Effect.NavigateBack -> onNavigateBack()
                is MovieDetailsContract.Effect.ShowMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
            }
        }
    }

    MovieDetailsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::setIntent,
    )
}

@Composable
fun MovieDetailsScreen(
    state: MovieDetailsContract.ViewState,
    snackbarHostState: SnackbarHostState,
    onIntent: (MovieDetailsContract.Intent) -> Unit,
) {
    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            DetailsTopBar(
                isRefreshing = state.isRefreshing,
                onBack = { onIntent(MovieDetailsContract.Intent.BackClicked) },
                onRefresh = { onIntent(MovieDetailsContract.Intent.Refresh) },
            )

            when {
                state.isLoading -> LoadingContent()
                state.movie != null -> MovieDetailsContent(
                    details = state.movie,
                    isFavorite = state.isFavorite,
                    isInWatchlist = state.isInWatchlist,
                    onFavoriteClicked = {
                        onIntent(MovieDetailsContract.Intent.FavoriteClicked)
                    },
                    onWatchlistClicked = {
                        onIntent(MovieDetailsContract.Intent.WatchlistClicked)
                    },
                )
                state.errorMessage != null -> ErrorContent(
                    message = state.errorMessage,
                    onRetry = { onIntent(MovieDetailsContract.Intent.Refresh) },
                    onDismiss = { onIntent(MovieDetailsContract.Intent.ErrorShown) },
                )
                else -> EmptyContent()
            }
        }
    }
}

@Composable
private fun DetailsTopBar(
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
        if (isRefreshing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "Refreshing",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else {
            OutlinedButton(onClick = onRefresh) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent() {
    Text(
        modifier = Modifier.padding(24.dp),
        text = "Movie details are not available yet.",
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRetry) {
                    Text("Retry")
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MovieDetailsContent(
    details: MovieDetails,
    isFavorite: Boolean,
    isInWatchlist: Boolean,
    onFavoriteClicked: () -> Unit,
    onWatchlistClicked: () -> Unit,
) {
    val movie = details.movie

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Backdrop(movie.backdropPath)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Poster(movie.posterPath)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = movie.metadataLine(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Ratings(movie)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                movie.genres.forEach { genre ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            text = genre.name,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onFavoriteClicked) {
                    Text(if (isFavorite) "Remove Favorite" else "Add Favorite")
                }
                OutlinedButton(onClick = onWatchlistClicked) {
                    Text(if (isInWatchlist) "Remove from Watchlist" else "Add to Watchlist")
                }
            }

            details.tagline?.takeIf { it.isNotBlank() }?.let { tagline ->
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = movie.overview?.takeIf { it.isNotBlank() } ?: "No overview available.",
                style = MaterialTheme.typography.bodyLarge,
            )

            CastSection(details.cast)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Backdrop(backdropPath: String?) {
    val backdropUrl = ImageUrlBuilder.backdropUrl(backdropPath)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (backdropUrl != null) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = backdropUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = "Backdrop",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Poster(posterPath: String?) {
    val posterUrl = ImageUrlBuilder.posterUrl(posterPath)

    Box(
        modifier = Modifier
            .size(width = 108.dp, height = 162.dp)
            .clip(RoundedCornerShape(8.dp))
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun Ratings(movie: Movie) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        movie.imdbRating?.let { rating ->
            Text(
                text = "IMDb ${rating.toDisplayText()} (${movie.imdbVotes ?: 0} votes)",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        movie.tmdbRating?.let { rating ->
            Text(
                text = "TMDb ${rating.toDisplayText()}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CastSection(cast: List<CastMember>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Cast",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (cast.isEmpty()) {
            Text(
                text = "Cast is not available.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            cast.take(12).forEach { member ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = member.department ?: member.professions ?: "Actor",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun Movie.metadataLine(): String =
    listOfNotNull(
        year?.toString(),
        runtime?.let { "$it min" },
    ).joinToString("  |  ").ifBlank { "Movie" }

private fun Double.toDisplayText(): String {
    val rounded = (this * 10).toInt() / 10.0
    return rounded.toString()
}
