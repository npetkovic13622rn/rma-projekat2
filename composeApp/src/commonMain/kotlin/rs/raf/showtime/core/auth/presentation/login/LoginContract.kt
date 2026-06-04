package rs.raf.showtime.core.auth.presentation.login

interface LoginContract {

    data class ViewState(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data class UsernameChanged(val username: String) : Intent
        data class PasswordChanged(val password: String) : Intent
        data object SubmitLogin : Intent
        data object ErrorShown : Intent
        data object GoToSignup : Intent
        data object Back : Intent
    }

    sealed interface Effect {
        data object NavigateToMain : Effect
        data object NavigateToSignup : Effect
        data object NavigateBack : Effect
        data class ShowMessage(val message: String) : Effect
    }
}
