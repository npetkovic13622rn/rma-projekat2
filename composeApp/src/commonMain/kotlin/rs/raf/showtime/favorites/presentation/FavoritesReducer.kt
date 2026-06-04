package rs.raf.showtime.favorites.presentation

object FavoritesReducer {
    fun reduce(
        state: FavoritesContract.ViewState,
        reducer: FavoritesContract.ViewState.() -> FavoritesContract.ViewState,
    ): FavoritesContract.ViewState = state.reducer()
}
