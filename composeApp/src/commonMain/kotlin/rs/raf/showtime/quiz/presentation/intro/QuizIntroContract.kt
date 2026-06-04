package rs.raf.showtime.quiz.presentation.intro

interface QuizIntroContract {

    data class ViewState(
        val canStartQuiz: Boolean = false,
        val isLoading: Boolean = true,
        val availableMovieCount: Int = 0,
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data object ScreenStarted : Intent
        data object StartQuizClicked : Intent
        data object ErrorShown : Intent
    }

    sealed interface Effect {
        data object NavigateToQuiz : Effect
        data class ShowMessage(val message: String) : Effect
    }
}
