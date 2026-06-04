package rs.raf.showtime.movies.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import rs.raf.showtime.movies.domain.Genre
import rs.raf.showtime.movies.domain.Movie
import rs.raf.showtime.movies.domain.MovieSortOption
import rs.raf.showtime.movies.domain.SortOrder
import rs.raf.showtime.movies.presentation.util.ImageUrlBuilder

@Composable
fun MoviesListRoute(
    onNavigateToMovieDetails: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MoviesListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.setIntent(MoviesListContract.Intent.ScreenStarted)
        viewModel.effects.collect { effect ->
            when (effect) {
                is MoviesListContract.Effect.NavigateToMovieDetails ->
                    onNavigateToMovieDetails(effect.movieId)
                is MoviesListContract.Effect.ShowMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
            }
        }
    }

    MoviesListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToWatchlist = onNavigateToWatchlist,
        onNavigateToQuiz = onNavigateToQuiz,
        onLogout = onLogout,
        onIntent = viewModel::setIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesListScreen(
    state: MoviesListContract.ViewState,
    snackbarHostState: SnackbarHostState,
    onNavigateToFavorites: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onLogout: () -> Unit,
    onIntent: (MoviesListContract.Intent) -> Unit,
) {
    var filtersExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Showtime",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToFavorites,
                    icon = {},
                    label = { Text("Favorites") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToWatchlist,
                    icon = {},
                    label = { Text("Watchlist") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToQuiz,
                    icon = {},
                    label = { Text("Quiz") },
                )
            }
        },
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
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                MoviesSearchAndFilters(
                    state = state,
                    filtersExpanded = filtersExpanded,
                    onToggleFilters = { filtersExpanded = !filtersExpanded },
                    onIntent = onIntent,
                )
            }

            item {
                MoviesListStatus(
                    state = state,
                    onIntent = onIntent,
                )
            }

            items(
                items = state.movies,
                key = { movie -> movie.movieId },
            ) { movie ->
                MovieListItemCard(
                    movie = movie,
                    onClick = {
                        onIntent(MoviesListContract.Intent.MovieClicked(movie.movieId))
                    },
                )
            }

            item {
                LoadMoreSection(
                    state = state,
                    onIntent = onIntent,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoviesSearchAndFilters(
    state: MoviesListContract.ViewState,
    filtersExpanded: Boolean,
    onToggleFilters: () -> Unit,
    onIntent: (MoviesListContract.Intent) -> Unit,
) {
    var minYearText by remember(state.filters.minYear) {
        mutableStateOf(state.filters.minYear?.toString().orEmpty())
    }
    var maxYearText by remember(state.filters.maxYear) {
        mutableStateOf(state.filters.maxYear?.toString().orEmpty())
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.filters.query,
                onValueChange = {
                    onIntent(MoviesListContract.Intent.QueryChanged(it))
                },
                singleLine = true,
                label = { Text("Search movies") },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    onClick = { onIntent(MoviesListContract.Intent.SearchSubmitted) },
                ) {
                    Text("Search")
                }
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    onClick = onToggleFilters,
                ) {
                    Text(if (filtersExpanded) "Hide filters" else "Filters")
                }
            }

            if (!filtersExpanded) {
                ActiveFiltersSummary(state)
                return@Column
            }

            HorizontalDivider()

            GenreSelector(
                modifier = Modifier.fillMaxWidth(),
                genres = state.genres,
                selectedGenreId = state.filters.genreId,
                onGenreSelected = {
                    onIntent(MoviesListContract.Intent.GenreSelected(it))
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = minYearText,
                    onValueChange = { value ->
                        minYearText = value.filter { it.isDigit() }.take(4)
                        onIntent(
                            MoviesListContract.Intent.YearRangeChanged(
                                minYear = minYearText.toIntOrNull(),
                                maxYear = maxYearText.toIntOrNull(),
                            ),
                        )
                    },
                    singleLine = true,
                    label = { Text("From") },
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = maxYearText,
                    onValueChange = { value ->
                        maxYearText = value.filter { it.isDigit() }.take(4)
                        onIntent(
                            MoviesListContract.Intent.YearRangeChanged(
                                minYear = minYearText.toIntOrNull(),
                                maxYear = maxYearText.toIntOrNull(),
                            ),
                        )
                    },
                    singleLine = true,
                    label = { Text("To") },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Minimum rating: ${state.filters.minRating?.toDisplayText() ?: "Any"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = state.filters.minRating?.toFloat() ?: 0f,
                    onValueChange = { value ->
                        onIntent(
                            MoviesListContract.Intent.MinRatingChanged(
                                value.takeIf { it > 0f }?.toDouble(),
                            ),
                        )
                    },
                    valueRange = 0f..10f,
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MovieSortOption.entries.forEach { option ->
                    FilterChip(
                        selected = state.filters.sortOption == option,
                        onClick = {
                            onIntent(
                                MoviesListContract.Intent.SortChanged(
                                    sortOption = option,
                                    sortOrder = state.filters.sortOrder,
                                ),
                            )
                        },
                        label = { Text(option.label) },
                    )
                }
                FilterChip(
                    selected = state.filters.sortOrder == SortOrder.Descending,
                    onClick = {
                        onIntent(
                            MoviesListContract.Intent.SortChanged(
                                sortOption = state.filters.sortOption,
                                sortOrder = if (state.filters.sortOrder == SortOrder.Descending) {
                                    SortOrder.Ascending
                                } else {
                                    SortOrder.Descending
                                },
                            ),
                        )
                    },
                    label = { Text(state.filters.sortOrder.label) },
                )
                OutlinedButton(
                    modifier = Modifier.heightIn(min = 48.dp),
                    onClick = { onIntent(MoviesListContract.Intent.ClearFilters) },
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
private fun ActiveFiltersSummary(state: MoviesListContract.ViewState) {
    val activeCount = listOfNotNull(
        state.filters.genreId,
        state.filters.minYear,
        state.filters.maxYear,
        state.filters.minRating,
        state.filters.query.takeIf { it.isNotBlank() },
    ).size

    Text(
        text = if (activeCount == 0) {
            "Showing popular movies"
        } else {
            "$activeCount active filter${if (activeCount == 1) "" else "s"}"
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun GenreSelector(
    modifier: Modifier = Modifier,
    genres: List<Genre>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGenre = genres.firstOrNull { it.id == selectedGenreId }

    Box(modifier = modifier) {
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            onClick = { expanded = true },
        ) {
            Text(selectedGenre?.name ?: "All genres")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All genres") },
                onClick = {
                    expanded = false
                    onGenreSelected(null)
                },
            )
            genres.forEach { genre ->
                DropdownMenuItem(
                    text = { Text(genre.name) },
                    onClick = {
                        expanded = false
                        onGenreSelected(genre.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun MoviesListStatus(
    state: MoviesListContract.ViewState,
    onIntent: (MoviesListContract.Intent) -> Unit,
) {
    when {
        state.isLoading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        state.errorMessage != null -> {
            ErrorMessage(
                message = state.errorMessage,
                onDismiss = { onIntent(MoviesListContract.Intent.ErrorShown) },
            )
        }
        state.emptyMessage != null -> {
            Text(
                text = state.emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        state.isRefreshing -> {
            Text(
                text = "Refreshing movies...",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        state.isOffline -> {
            Text(
                text = "Showing locally cached movies.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
}

@Composable
private fun MovieListItemCard(
    movie: Movie,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MoviePoster(movie.posterPath)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
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
                if (movie.genres.isNotEmpty()) {
                    Text(
                        text = movie.genres.take(3).joinToString { it.name },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoviePoster(posterPath: String?) {
    val posterUrl = ImageUrlBuilder.posterUrl(posterPath)

    Box(
        modifier = Modifier
            .size(width = 84.dp, height = 126.dp)
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
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoadMoreSection(
    state: MoviesListContract.ViewState,
    onIntent: (MoviesListContract.Intent) -> Unit,
) {
    if (state.movies.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isLoadingNextPage) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Loading more")
        } else {
            Button(
                enabled = state.canLoadMore,
                onClick = { onIntent(MoviesListContract.Intent.LoadNextPage) },
            ) {
                Text("Load more")
            }
        }
    }
}

private val MovieSortOption.label: String
    get() = when (this) {
        MovieSortOption.Rating -> "Rating"
        MovieSortOption.Year -> "Year"
        MovieSortOption.Title -> "Title"
    }

private val SortOrder.label: String
    get() = when (this) {
        SortOrder.Ascending -> "Ascending"
        SortOrder.Descending -> "Descending"
    }

private fun Double.toDisplayText(): String {
    val rounded = (this * 10).toInt() / 10.0
    return rounded.toString()
}
