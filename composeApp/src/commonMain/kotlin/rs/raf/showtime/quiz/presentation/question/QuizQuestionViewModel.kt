package rs.raf.showtime.quiz.presentation.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.raf.showtime.core.util.RepositoryException
import rs.raf.showtime.quiz.domain.QuizException
import rs.raf.showtime.quiz.domain.QuizRepository
import rs.raf.showtime.quiz.domain.QuizResult
import rs.raf.showtime.quiz.domain.QuizRules
import rs.raf.showtime.quiz.domain.QuizSession
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class QuizQuestionViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizQuestionContract.ViewState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<QuizQuestionContract.Effect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    private var session: QuizSession? = null
    private var hasStarted = false
    private var hasFinished = false
    private var timerJob: Job? = null

    fun setIntent(intent: QuizQuestionContract.Intent) {
        when (intent) {
            QuizQuestionContract.Intent.ScreenStarted -> onScreenStarted()
            is QuizQuestionContract.Intent.AnswerClicked -> answer(intent.answerId)
            QuizQuestionContract.Intent.NextQuestion -> nextQuestion()
            QuizQuestionContract.Intent.TimerTick -> timerTick()
            QuizQuestionContract.Intent.TimeExpired -> finishQuiz(remainingSeconds = 0)
            QuizQuestionContract.Intent.BackClicked ->
                setState { copy(showAbandonDialog = true) }
            QuizQuestionContract.Intent.ConfirmAbandon -> {
                timerJob?.cancel()
                _effects.tryEmit(QuizQuestionContract.Effect.NavigateBack)
            }
            QuizQuestionContract.Intent.DismissAbandonDialog ->
                setState { copy(showAbandonDialog = false) }
        }
    }

    private fun onScreenStarted() {
        if (hasStarted) return
        hasStarted = true

        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            runCatching {
                quizRepository.startQuiz()
            }.onSuccess { startedSession ->
                session = startedSession
                setState {
                    copy(
                        currentQuestion = startedSession.questions.firstOrNull(),
                        totalQuestions = startedSession.questions.size,
                        remainingSeconds = startedSession.totalSeconds,
                        isLoading = false,
                    )
                }
                startTimer()
            }.onFailure { error ->
                if (error is CancellationException) throw error
                val message = error.asUserMessage("Unable to start quiz.")
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = message,
                        isFinishing = true,
                    )
                }
                _effects.tryEmit(QuizQuestionContract.Effect.ShowMessage(message))
            }
        }
    }

    private fun answer(answerId: String) {
        val snapshot = _state.value
        val question = snapshot.currentQuestion ?: return
        if (snapshot.isAnswerLocked || snapshot.isFinishing) return

        val isCorrect = answerId == question.correctAnswerId
        setState {
            copy(
                selectedAnswerId = answerId,
                correctAnswerId = question.correctAnswerId,
                isAnswerLocked = true,
                correctCount = correctCount + if (isCorrect) 1 else 0,
                incorrectCount = incorrectCount + if (isCorrect) 0 else 1,
            )
        }

        viewModelScope.launch {
            delay(850)
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        val activeSession = session ?: return
        val snapshot = _state.value
        if (snapshot.isFinishing) return

        val nextIndex = snapshot.questionIndex + 1
        if (nextIndex >= activeSession.questions.size) {
            finishQuiz(remainingSeconds = snapshot.remainingSeconds)
            return
        }

        setState {
            copy(
                questionIndex = nextIndex,
                currentQuestion = activeSession.questions[nextIndex],
                selectedAnswerId = null,
                correctAnswerId = null,
                isAnswerLocked = false,
            )
        }
    }

    private fun timerTick() {
        val snapshot = _state.value
        if (snapshot.isLoading || snapshot.isFinishing) return
        if (snapshot.remainingSeconds <= 1) {
            finishQuiz(remainingSeconds = 0)
        } else {
            setState { copy(remainingSeconds = remainingSeconds - 1) }
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val snapshot = _state.value
                if (snapshot.isFinishing || snapshot.currentQuestion == null) return@launch
                timerTick()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun finishQuiz(remainingSeconds: Int) {
        val snapshot = _state.value
        if (snapshot.isFinishing || hasFinished) return
        hasFinished = true
        timerJob?.cancel()

        val correctAnswers = snapshot.correctCount
        val incorrectAnswers = snapshot.totalQuestions - correctAnswers
        val usedTimeSeconds = QuizRules.TotalSeconds - remainingSeconds.coerceIn(0, QuizRules.TotalSeconds)
        val result = QuizResult(
            score = QuizRules.calculateScore(
                correctAnswers = correctAnswers,
                remainingSeconds = remainingSeconds,
            ),
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            usedTimeSeconds = usedTimeSeconds,
            createdAt = Clock.System.now().toEpochMilliseconds(),
        )

        setState {
            copy(
                isFinishing = true,
                incorrectCount = incorrectAnswers,
            )
        }

        viewModelScope.launch {
            runCatching {
                quizRepository.saveQuizResult(result)
            }.onFailure { error ->
                if (error is CancellationException) throw error
                _effects.tryEmit(
                    QuizQuestionContract.Effect.ShowMessage(
                        error.asUserMessage("Quiz result could not be saved locally."),
                    ),
                )
            }
            _effects.tryEmit(QuizQuestionContract.Effect.NavigateToResult(result))
        }
    }

    private fun Throwable.asUserMessage(defaultMessage: String): String =
        when (this) {
            is QuizException -> message ?: defaultMessage
            is RepositoryException -> message ?: defaultMessage
            else -> message ?: defaultMessage
        }

    private fun setState(reducer: QuizQuestionContract.ViewState.() -> QuizQuestionContract.ViewState) {
        _state.getAndUpdate { state -> QuizQuestionReducer.reduce(state, reducer) }
    }
}
