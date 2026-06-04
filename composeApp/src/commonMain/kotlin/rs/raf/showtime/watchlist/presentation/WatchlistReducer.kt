package rs.raf.showtime.watchlist.presentation

object WatchlistReducer {
    fun reduce(
        state: WatchlistContract.ViewState,
        reducer: WatchlistContract.ViewState.() -> WatchlistContract.ViewState,
    ): WatchlistContract.ViewState = state.reducer()
}
