package rs.raf.showtime.quiz.presentation.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.showtime.quiz.domain.QuizRepository
import rs.raf.showtime.quiz.domain.QuizRules

class QuizIntroViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizIntroContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<QuizIntroContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    private var hasStarted = false

    fun setIntent(intent: QuizIntroContract.Intent) {
        when (intent) {
            QuizIntroContract.Intent.ScreenStarted -> onScreenStarted()
            QuizIntroContract.Intent.StartQuizClicked -> startQuiz()
            QuizIntroContract.Intent.ErrorShown -> setState { copy(errorMessage = null) }
        }
    }

    private fun onScreenStarted() {
        if (hasStarted) return
        hasStarted = true
        refreshPoolStatus()
    }

    private fun refreshPoolStatus() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            runCatching {
                quizRepository.getAvailableMovieCount()
            }.onSuccess { count ->
                setState {
                    copy(
                        availableMovieCount = count,
                        canStartQuiz = count >= QuizRules.QuestionCount,
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                val message = error.message ?: "Unable to check quiz pool."
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = message,
                    )
                }
                _effects.tryEmit(QuizIntroContract.Effect.ShowMessage(message))
            }
        }
    }

    private fun startQuiz() {
        val state = _state.value
        if (state.canStartQuiz) {
            _effects.tryEmit(QuizIntroContract.Effect.NavigateToQuiz)
        } else {
            val message = "Browse the catalog first to populate your quiz pool."
            setState { copy(errorMessage = message) }
            _effects.tryEmit(QuizIntroContract.Effect.ShowMessage(message))
        }
    }

    private fun setState(reducer: QuizIntroContract.ViewState.() -> QuizIntroContract.ViewState) {
        _state.getAndUpdate(reducer)
    }
}
