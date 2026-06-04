package rs.raf.showtime.core.auth.presentation.signup

object SignupReducer {
    fun reduce(
        state: SignupContract.ViewState,
        reducer: SignupContract.ViewState.() -> SignupContract.ViewState,
    ): SignupContract.ViewState = state.reducer()
}
