package rs.raf.showtime.quiz.presentation.intro

object QuizIntroReducer {
    fun reduce(
        state: QuizIntroContract.ViewState,
        reducer: QuizIntroContract.ViewState.() -> QuizIntroContract.ViewState,
    ): QuizIntroContract.ViewState = state.reducer()
}
