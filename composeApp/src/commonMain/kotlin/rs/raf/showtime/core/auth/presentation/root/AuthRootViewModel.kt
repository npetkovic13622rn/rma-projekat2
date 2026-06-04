package rs.raf.showtime.core.auth.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rs.raf.showtime.core.auth.domain.AuthRepository
import rs.raf.showtime.core.auth.model.AuthState

class AuthRootViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.observeAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.CheckingSession,
        )

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
