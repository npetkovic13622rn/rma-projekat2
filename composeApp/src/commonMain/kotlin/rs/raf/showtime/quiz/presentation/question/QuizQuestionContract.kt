package rs.raf.showtime.quiz.presentation.question

import rs.raf.showtime.quiz.domain.QuizQuestion
import rs.raf.showtime.quiz.domain.QuizResult
import rs.raf.showtime.quiz.domain.QuizRules

interface QuizQuestionContract {

    data class ViewState(
        val currentQuestion: QuizQuestion? = null,
        val questionIndex: Int = 0,
        val totalQuestions: Int = QuizRules.QuestionCount,
        val remainingSeconds: Int = QuizRules.TotalSeconds,
        val selectedAnswerId: String? = null,
        val correctAnswerId: String? = null,
        val isAnswerLocked: Boolean = false,
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
        val showAbandonDialog: Boolean = false,
        val isFinishing: Boolean = false,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data object ScreenStarted : Intent
        data class AnswerClicked(val answerId: String) : Intent
        data object NextQuestion : Intent
        data object TimerTick : Intent
        data object TimeExpired : Intent
        data object BackClicked : Intent
        data object ConfirmAbandon : Intent
        data object DismissAbandonDialog : Intent
    }

    sealed interface Effect {
        data class NavigateToResult(val result: QuizResult) : Effect
        data object NavigateBack : Effect
        data class ShowMessage(val message: String) : Effect
    }
}
