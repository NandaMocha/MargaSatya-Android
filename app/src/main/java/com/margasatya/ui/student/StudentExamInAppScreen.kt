package com.margasatya.ui.student

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.margasatya.domain.enums.QuestionType
import com.margasatya.ui.MainActivity
import com.margasatya.ui.components.ConfirmDialog
import com.margasatya.ui.components.ErrorDialog
import com.margasatya.ui.components.LoadingDialog
import com.margasatya.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentExamInAppScreen(
    navController: NavController,
    examId: String,
    sessionId: String,
    viewModel: StudentExamInAppViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? MainActivity

    // Handle lock mode
    LaunchedEffect(Unit) {
        activity?.let {
            viewModel.loadExam(examId, sessionId)
        }
    }

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            activity?.let {
                // Lock will be released
            }
            navController.navigate(Screen.RoleSelection.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(uiState.isSubmissionPending) {
        if (uiState.isSubmissionPending) {
            activity?.let {
                // Lock released, go to pending screen
            }
            navController.navigate(Screen.StudentExamSubmissionPending.createRoute(sessionId)) {
                popUpTo(Screen.StudentEntry.route) { inclusive = true }
            }
        }
    }

    BackHandler {
        // Prevent back navigation during exam
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ujian In-App")
                        if (!uiState.isLoading && uiState.questions.isNotEmpty()) {
                            Text(
                                text = "Soal ${uiState.currentQuestionIndex + 1} dari ${uiState.questions.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.questions.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousQuestion() },
                            enabled = uiState.currentQuestionIndex > 0
                        ) {
                            Text("Sebelumnya")
                        }

                        if (uiState.currentQuestionIndex < uiState.questions.size - 1) {
                            Button(onClick = { viewModel.nextQuestion() }) {
                                Text("Berikutnya")
                            }
                        } else {
                            Button(onClick = { viewModel.showSubmitDialog() }) {
                                Text("Kumpulkan Ujian")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!uiState.isLoading && uiState.questions.isNotEmpty()) {
                // Question navigation indicators
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.questions) { index, question ->
                        val isAnswered = uiState.answers[question.id]?.isAnswered == true
                        val isCurrent = index == uiState.currentQuestionIndex

                        FilterChip(
                            selected = isCurrent,
                            onClick = { viewModel.goToQuestion(index) },
                            label = {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = if (isAnswered) {
                                {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                        contentDescription = "Sudah dijawab",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }

                Divider()

                // Question content
                val currentQuestion = viewModel.getCurrentQuestion()
                if (currentQuestion != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        when (currentQuestion.type) {
                            QuestionType.MULTIPLE_CHOICE -> {
                                MultipleChoiceQuestion(
                                    options = currentQuestion.options,
                                    selectedAnswer = viewModel.getCurrentAnswer(),
                                    onAnswerSelected = { answer ->
                                        viewModel.updateAnswer(currentQuestion.id, answer)
                                    }
                                )
                            }
                            QuestionType.ESSAY -> {
                                EssayQuestion(
                                    answer = viewModel.getCurrentAnswer(),
                                    onAnswerChanged = { answer ->
                                        viewModel.updateAnswer(currentQuestion.id, answer)
                                    }
                                )
                            }
                        }

                        if (uiState.isSaving) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isLoading) {
            LoadingDialog(message = "Memuat soal...")
        }

        if (uiState.showSubmitDialog) {
            val unansweredCount = uiState.answers.values.count { !it.isAnswered }
            ConfirmDialog(
                title = "Kumpulkan Ujian",
                message = if (unansweredCount > 0) {
                    "Masih ada $unansweredCount soal yang belum dijawab. Apakah kamu yakin ingin mengumpulkan ujian?"
                } else {
                    "Apakah kamu yakin ingin mengumpulkan ujian?"
                },
                onConfirm = { viewModel.submitExam() },
                onDismiss = { viewModel.hideSubmitDialog() }
            )
        }

        if (uiState.error != null) {
            ErrorDialog(
                message = uiState.error!!,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

@Composable
fun MultipleChoiceQuestion(
    options: List<String>,
    selectedAnswer: String,
    onAnswerSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = if (selectedAnswer == option) {
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    CardDefaults.cardColors()
                },
                onClick = { onAnswerSelected(option) }
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun EssayQuestion(
    answer: String,
    onAnswerChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = answer,
        onValueChange = onAnswerChanged,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        placeholder = { Text("Tuliskan jawaban kamu di sini...") },
        maxLines = 10
    )
}
