package rs.raf.showtime.quiz.presentation.question

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.ui.backhandler.BackHandler
import org.koin.compose.viewmodel.koinViewModel
import rs.raf.showtime.movies.presentation.util.ImageUrlBuilder
import rs.raf.showtime.quiz.domain.QuizAnswer
import rs.raf.showtime.quiz.domain.QuizQuestion
import rs.raf.showtime.quiz.domain.QuizResult

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun QuizQuestionRoute(
    onNavigateToResult: (QuizResult) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: QuizQuestionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        viewModel.setIntent(QuizQuestionContract.Intent.BackClicked)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is QuizQuestionContract.Effect.NavigateToResult ->
                    onNavigateToResult(effect.result)
                QuizQuestionContract.Effect.NavigateBack -> onNavigateBack()
                is QuizQuestionContract.Effect.ShowMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.setIntent(QuizQuestionContract.Intent.ScreenStarted)
    }

    QuizQuestionScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::setIntent,
    )
}

@Composable
fun QuizQuestionScreen(
    state: QuizQuestionContract.ViewState,
    snackbarHostState: SnackbarHostState,
    onIntent: (QuizQuestionContract.Intent) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.currentQuestion == null -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.errorMessage ?: "Preparing quiz...")
                    if (state.errorMessage != null) {
                        OutlinedButton(
                            onClick = {
                                onIntent(QuizQuestionContract.Intent.ConfirmAbandon)
                            },
                        ) {
                            Text("Back to quiz")
                        }
                    }
                }
                else -> QuizQuestionContent(
                    state = state,
                    question = state.currentQuestion,
                    onIntent = onIntent,
                )
            }
        }
    }

    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = {
                onIntent(QuizQuestionContract.Intent.DismissAbandonDialog)
            },
            title = { Text("Abandon quiz?") },
            text = { Text("Your progress will be lost.") },
            confirmButton = {
                Button(
                    onClick = { onIntent(QuizQuestionContract.Intent.ConfirmAbandon) },
                ) {
                    Text("Abandon")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { onIntent(QuizQuestionContract.Intent.DismissAbandonDialog) },
                ) {
                    Text("Continue")
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuizQuestionContent(
    state: QuizQuestionContract.ViewState,
    question: QuizQuestion,
    onIntent: (QuizQuestionContract.Intent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Question ${state.questionIndex + 1}/${state.totalQuestions}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${state.remainingSeconds}s",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        QuizImage(path = question.imagePath)

        Text(
            text = question.prompt,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            question.answers.forEach { answer ->
                AnswerButton(
                    answer = answer,
                    state = state,
                    onClick = {
                        onIntent(QuizQuestionContract.Intent.AnswerClicked(answer.id))
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun QuizImage(path: String?) {
    val imageUrl = ImageUrlBuilder.backdropUrl(path)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = "Image",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AnswerButton(
    answer: QuizAnswer,
    state: QuizQuestionContract.ViewState,
    onClick: () -> Unit,
) {
    val isSelected = state.selectedAnswerId == answer.id
    val isCorrect = state.correctAnswerId == answer.id
    val color = when {
        state.isAnswerLocked && isCorrect -> MaterialTheme.colorScheme.primaryContainer
        state.isAnswerLocked && isSelected -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        state.isAnswerLocked && isCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
        state.isAnswerLocked && isSelected -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(min = 260.dp, max = 340.dp),
        color = color,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        onClick = onClick,
        enabled = !state.isAnswerLocked && !state.isFinishing,
    ) {
        Text(
            modifier = Modifier.padding(14.dp),
            text = answer.text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
