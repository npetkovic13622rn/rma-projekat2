package rs.raf.showtime.quiz.presentation.question

object QuizQuestionReducer {
    fun reduce(
        state: QuizQuestionContract.ViewState,
        reducer: QuizQuestionContract.ViewState.() -> QuizQuestionContract.ViewState,
    ): QuizQuestionContract.ViewState = state.reducer()
}
