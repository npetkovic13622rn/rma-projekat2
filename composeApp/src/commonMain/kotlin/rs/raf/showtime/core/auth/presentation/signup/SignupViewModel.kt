package rs.raf.showtime.core.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.showtime.core.auth.domain.AuthException
import rs.raf.showtime.core.auth.domain.AuthRepository

class SignupViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignupContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SignupContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    fun setIntent(intent: SignupContract.Intent) {
        when (intent) {
            is SignupContract.Intent.FullNameChanged ->
                setState { copy(fullName = intent.fullName, errorMessage = null) }
            is SignupContract.Intent.UsernameChanged ->
                setState { copy(username = intent.username, errorMessage = null) }
            is SignupContract.Intent.PasswordChanged ->
                setState { copy(password = intent.password, errorMessage = null) }
            SignupContract.Intent.SubmitSignup -> submitSignup()
            SignupContract.Intent.ErrorShown -> setState { copy(errorMessage = null) }
            SignupContract.Intent.GoToLogin ->
                _effects.tryEmit(SignupContract.Effect.NavigateToLogin)
            SignupContract.Intent.Back ->
                _effects.tryEmit(SignupContract.Effect.NavigateBack)
        }
    }

    private fun submitSignup() {
        val snapshot = _state.value
        val username = snapshot.username.trim()
        val validationError = when {
            snapshot.fullName.isBlank() -> "Full name is required."
            !UsernameRegex.matches(username) ->
                "Username must have at least 3 letters, digits, or underscores."
            snapshot.password.length < 8 -> "Password must have at least 8 characters."
            else -> null
        }

        if (validationError != null) {
            setState { copy(errorMessage = validationError) }
            _effects.tryEmit(SignupContract.Effect.ShowMessage(validationError))
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            runCatching {
                authRepository.signup(
                    fullName = snapshot.fullName,
                    username = snapshot.username,
                    password = snapshot.password,
                )
            }
                .onSuccess {
                    setState { copy(isLoading = false) }
                    _effects.tryEmit(SignupContract.Effect.NavigateToMain)
                }
                .onFailure { error ->
                    val message = error.asUserMessage()
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = message,
                        )
                    }
                    _effects.tryEmit(SignupContract.Effect.ShowMessage(message))
                }
        }
    }

    private fun Throwable.asUserMessage(): String =
        when (this) {
            is AuthException -> message ?: "Signup failed."
            else -> message ?: "Signup failed."
        }

    private fun setState(reducer: SignupContract.ViewState.() -> SignupContract.ViewState) {
        _state.getAndUpdate(reducer)
    }

    private companion object {
        val UsernameRegex = Regex("^[A-Za-z0-9_]{3,}$")
    }
}
