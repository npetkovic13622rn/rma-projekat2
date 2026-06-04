package rs.raf.showtime.movies.presentation.details

object MovieDetailsReducer {
    fun reduce(
        state: MovieDetailsContract.ViewState,
        reducer: MovieDetailsContract.ViewState.() -> MovieDetailsContract.ViewState,
    ): MovieDetailsContract.ViewState = state.reducer()
}
