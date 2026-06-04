package rs.raf.showtime.core.auth.presentation.login

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

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LoginContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    fun setIntent(intent: LoginContract.Intent) {
        when (intent) {
            is LoginContract.Intent.UsernameChanged ->
                setState { copy(username = intent.username, errorMessage = null) }
            is LoginContract.Intent.PasswordChanged ->
                setState { copy(password = intent.password, errorMessage = null) }
            LoginContract.Intent.SubmitLogin -> submitLogin()
            LoginContract.Intent.ErrorShown -> setState { copy(errorMessage = null) }
            LoginContract.Intent.GoToSignup ->
                _effects.tryEmit(LoginContract.Effect.NavigateToSignup)
            LoginContract.Intent.Back ->
                _effects.tryEmit(LoginContract.Effect.NavigateBack)
        }
    }

    private fun submitLogin() {
        val snapshot = _state.value
        val validationError = when {
            snapshot.username.isBlank() -> "Username is required."
            snapshot.password.isBlank() -> "Password is required."
            else -> null
        }

        if (validationError != null) {
            setState { copy(errorMessage = validationError) }
            _effects.tryEmit(LoginContract.Effect.ShowMessage(validationError))
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            runCatching {
                authRepository.login(
                    username = snapshot.username,
                    password = snapshot.password,
                )
            }
                .onSuccess {
                    setState { copy(isLoading = false) }
                    _effects.tryEmit(LoginContract.Effect.NavigateToMain)
                }
                .onFailure { error ->
                    val message = error.asUserMessage()
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = message,
                        )
                    }
                    _effects.tryEmit(LoginContract.Effect.ShowMessage(message))
                }
        }
    }

    private fun Throwable.asUserMessage(): String =
        when (this) {
            is AuthException -> message ?: "Login failed."
            else -> message ?: "Login failed."
        }

    private fun setState(reducer: LoginContract.ViewState.() -> LoginContract.ViewState) {
        _state.getAndUpdate(reducer)
    }
}
