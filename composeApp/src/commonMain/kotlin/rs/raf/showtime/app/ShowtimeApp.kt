package rs.raf.showtime.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import rs.raf.showtime.core.auth.model.AuthState
import rs.raf.showtime.core.auth.presentation.landing.AuthLandingRoute
import rs.raf.showtime.core.auth.presentation.login.LoginRoute
import rs.raf.showtime.core.auth.presentation.root.AuthRootViewModel
import rs.raf.showtime.core.auth.presentation.signup.SignupRoute
import rs.raf.showtime.favorites.presentation.FavoritesRoute
import rs.raf.showtime.movies.presentation.details.MovieDetailsRoute
import rs.raf.showtime.movies.presentation.list.MoviesListRoute
import rs.raf.showtime.quiz.domain.QuizResult
import rs.raf.showtime.quiz.presentation.intro.QuizIntroRoute
import rs.raf.showtime.quiz.presentation.question.QuizQuestionRoute
import rs.raf.showtime.quiz.presentation.result.QuizResultRoute
import rs.raf.showtime.watchlist.presentation.WatchlistRoute

@Composable
fun ShowtimeApp(
    authRootViewModel: AuthRootViewModel = koinViewModel(),
) {
    val authState by authRootViewModel.authState.collectAsState()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (authState) {
                AuthState.CheckingSession -> CheckingSessionScreen()
                is AuthState.Authenticated -> AuthenticatedGraph(
                    onLogout = authRootViewModel::logout,
                )
                AuthState.Unauthenticated -> UnauthenticatedGraph()
            }
        }
    }
}

@Composable
private fun CheckingSessionScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun UnauthenticatedGraph() {
    var route by remember { mutableStateOf(AuthRoute.Landing) }

    when (route) {
        AuthRoute.Landing -> AuthLandingRoute(
            onNavigateToLogin = { route = AuthRoute.Login },
            onNavigateToSignup = { route = AuthRoute.Signup },
        )
        AuthRoute.Login -> LoginRoute(
            onNavigateToMain = { route = AuthRoute.Landing },
            onNavigateToSignup = { route = AuthRoute.Signup },
            onNavigateBack = { route = AuthRoute.Landing },
        )
        AuthRoute.Signup -> SignupRoute(
            onNavigateToMain = { route = AuthRoute.Landing },
            onNavigateToLogin = { route = AuthRoute.Login },
            onNavigateBack = { route = AuthRoute.Landing },
        )
    }
}

@Composable
private fun AuthenticatedGraph(
    onLogout: () -> Unit,
) {
    var route by remember { mutableStateOf(MainRoute.Movies) }
    var selectedMovieId by remember { mutableStateOf<String?>(null) }
    var latestQuizResult by remember { mutableStateOf<QuizResult?>(null) }

    LaunchedEffect(Unit) {
        selectedMovieId = null
    }

    selectedMovieId?.let { movieId ->
        MovieDetailsRoute(
            movieId = movieId,
            onNavigateBack = { selectedMovieId = null },
        )
    } ?: when (route) {
        MainRoute.Movies -> MoviesListRoute(
            onNavigateToMovieDetails = { movieId ->
                selectedMovieId = movieId
            },
            onNavigateToFavorites = { route = MainRoute.Favorites },
            onNavigateToWatchlist = { route = MainRoute.Watchlist },
            onNavigateToQuiz = { route = MainRoute.QuizIntro },
            onLogout = onLogout,
        )
        MainRoute.Favorites -> FavoritesRoute(
            onNavigateToMovieDetails = { movieId ->
                selectedMovieId = movieId
            },
            onNavigateBack = { route = MainRoute.Movies },
        )
        MainRoute.Watchlist -> WatchlistRoute(
            onNavigateToMovieDetails = { movieId ->
                selectedMovieId = movieId
            },
            onNavigateBack = { route = MainRoute.Movies },
        )
        MainRoute.QuizIntro -> QuizIntroRoute(
            onNavigateToQuiz = { route = MainRoute.QuizPlay },
            onNavigateBack = { route = MainRoute.Movies },
        )
        MainRoute.QuizPlay -> QuizQuestionRoute(
            onNavigateToResult = { result ->
                latestQuizResult = result
                route = MainRoute.QuizResult
            },
            onNavigateBack = { route = MainRoute.QuizIntro },
        )
        MainRoute.QuizResult -> latestQuizResult?.let { result ->
            QuizResultRoute(
                result = result,
                onNavigateToMovies = { route = MainRoute.Movies },
                onPlayAgain = { route = MainRoute.QuizPlay },
            )
        } ?: QuizIntroRoute(
            onNavigateToQuiz = { route = MainRoute.QuizPlay },
            onNavigateBack = { route = MainRoute.Movies },
        )
    }
}

private enum class AuthRoute {
    Landing,
    Login,
    Signup,
}

private enum class MainRoute {
    Movies,
    Favorites,
    Watchlist,
    QuizIntro,
    QuizPlay,
    QuizResult,
}
