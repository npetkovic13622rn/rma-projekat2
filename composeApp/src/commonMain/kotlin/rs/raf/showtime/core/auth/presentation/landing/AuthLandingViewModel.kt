package rs.raf.showtime.core.auth.presentation.landing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthLandingViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthLandingContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AuthLandingContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    fun setIntent(intent: AuthLandingContract.Intent) {
        when (intent) {
            AuthLandingContract.Intent.LoginClicked ->
                _effects.tryEmit(AuthLandingContract.Effect.NavigateToLogin)
            AuthLandingContract.Intent.SignupClicked ->
                _effects.tryEmit(AuthLandingContract.Effect.NavigateToSignup)
        }
    }
}
