package rs.raf.showtime.quiz.presentation.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rs.raf.showtime.quiz.domain.QuizResult

@Composable
fun QuizResultRoute(
    result: QuizResult,
    onNavigateToMovies: () -> Unit,
    onPlayAgain: () -> Unit,
) {
    QuizResultScreen(
        result = result,
        onNavigateToMovies = onNavigateToMovies,
        onPlayAgain = onPlayAgain,
    )
}

@Composable
fun QuizResultScreen(
    result: QuizResult,
    onNavigateToMovies: () -> Unit,
    onPlayAgain: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Quiz Result",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                ResultLine(label = "Score", value = result.score.formatScore())
                ResultLine(label = "Correct answers", value = result.correctAnswers.toString())
                ResultLine(label = "Incorrect answers", value = result.incorrectAnswers.toString())
                ResultLine(label = "Used time", value = "${result.usedTimeSeconds}s")
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPlayAgain,
                ) {
                    Text("Play again")
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToMovies,
                ) {
                    Text("Back to movies")
                }
            }
        }
    }
}

@Composable
private fun ResultLine(
    label: String,
    value: String,
) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.titleMedium,
    )
}

private fun Double.formatScore(): String {
    val rounded = (this * 100).toInt() / 100.0
    val text = rounded.toString()
    val decimals = text.substringAfter('.', "")
    return when (decimals.length) {
        0 -> "$text.00"
        1 -> "${text}0"
        else -> text
    }
}
