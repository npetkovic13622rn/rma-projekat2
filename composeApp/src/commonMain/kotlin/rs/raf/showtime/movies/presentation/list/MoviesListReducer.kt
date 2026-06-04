package rs.raf.showtime.movies.presentation.list

object MoviesListReducer {
    fun reduce(
        state: MoviesListContract.ViewState,
        reducer: MoviesListContract.ViewState.() -> MoviesListContract.ViewState,
    ): MoviesListContract.ViewState = state.reducer()
}
