package rs.raf.showtime.quiz.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class QuizRulesTest {

    @Test
    fun scoreIsZeroWhenNoAnswersAreCorrect() {
        assertEquals(
            expected = 0.0,
            actual = QuizRules.calculateScore(correctAnswers = 0, remainingSeconds = 60),
        )
    }

    @Test
    fun perfectScoreIsCappedAtOneHundred() {
        assertEquals(
            expected = 100.0,
            actual = QuizRules.calculateScore(correctAnswers = 10, remainingSeconds = 60),
        )
    }

    @Test
    fun scoreUsesRemainingTimeBonus() {
        assertEquals(
            expected = 47.5,
            actual = QuizRules.calculateScore(correctAnswers = 5, remainingSeconds = 30),
        )
    }
}
