package rs.raf.showtime.core.auth.presentation.signup

interface SignupContract {

    data class ViewState(
        val fullName: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data class FullNameChanged(val fullName: String) : Intent
        data class UsernameChanged(val username: String) : Intent
        data class PasswordChanged(val password: String) : Intent
        data object SubmitSignup : Intent
        data object ErrorShown : Intent
        data object GoToLogin : Intent
        data object Back : Intent
    }

    sealed interface Effect {
        data object NavigateToMain : Effect
        data object NavigateToLogin : Effect
        data object NavigateBack : Effect
        data class ShowMessage(val message: String) : Effect
    }
}
