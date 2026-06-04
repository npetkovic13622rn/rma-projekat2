package rs.raf.showtime.core.auth.presentation.landing

interface AuthLandingContract {

    data class ViewState(
        val appTitle: String = "Showtime",
    )

    sealed interface Intent {
        data object LoginClicked : Intent
        data object SignupClicked : Intent
    }

    sealed interface Effect {
        data object NavigateToLogin : Effect
        data object NavigateToSignup : Effect
    }
}
