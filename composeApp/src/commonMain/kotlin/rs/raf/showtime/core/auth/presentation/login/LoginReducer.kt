package rs.raf.showtime.core.auth.presentation.login

object LoginReducer {
    fun reduce(
        state: LoginContract.ViewState,
        reducer: LoginContract.ViewState.() -> LoginContract.ViewState,
    ): LoginContract.ViewState = state.reducer()
}
