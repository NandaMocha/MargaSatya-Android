package com.margasatya.ui.student

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.margasatya.core.lock.ExamLockManager
import com.margasatya.data.service.ExamService
import com.margasatya.data.service.ExamSessionService
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.ui.MainActivity
import com.margasatya.ui.components.ConfirmDialog
import com.margasatya.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WebViewExamUiState(
    val formUrl: String? = null,
    val isLoading: Boolean = true,
    val showConfirmDialog: Boolean = false,
    val isSubmitted: Boolean = false
)

@HiltViewModel
class StudentExamWebViewViewModel @Inject constructor(
    private val examService: ExamService,
    private val examSessionService: ExamSessionService,
    private val examLockManager: ExamLockManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WebViewExamUiState())
    val uiState: StateFlow<WebViewExamUiState> = _uiState.asStateFlow()

    fun loadExam(examId: String) {
        viewModelScope.launch {
            try {
                val exam = examService.getExamByCode("")  // Get by ID instead
                _uiState.value = _uiState.value.copy(
                    formUrl = exam?.formUrl,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun showConfirmDialog() {
        _uiState.value = _uiState.value.copy(showConfirmDialog = true)
    }

    fun hideConfirmDialog() {
        _uiState.value = _uiState.value.copy(showConfirmDialog = false)
    }

    fun submitExam(sessionId: String) {
        viewModelScope.launch {
            try {
                examSessionService.updateSessionStatus(sessionId, ExamSessionStatus.SUBMITTED)
                _uiState.value = _uiState.value.copy(isSubmitted = true)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentExamWebViewScreen(
    navController: NavController,
    examId: String,
    sessionId: String,
    viewModel: StudentExamWebViewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? MainActivity

    // Handle lock mode
    LaunchedEffect(Unit) {
        activity?.let {
            val success = viewModel.examLockManager.startExamLock(it)
            if (!success) {
                // Show error and go back
                navController.navigateUp()
            }
        }
        viewModel.loadExam(examId)
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.let {
                viewModel.examLockManager.stopExamLock(it)
            }
        }
    }

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            activity?.let {
                viewModel.examLockManager.stopExamLock(it)
            }
            navController.navigate(Screen.RoleSelection.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    BackHandler {
        // Prevent back navigation during exam
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ujian Google Form") }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { viewModel.showConfirmDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Selesai / Kirim Ujian")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pastikan kamu sudah menekan tombol Kirim di Google Form, lalu tekan 'Selesai / Kirim Ujian' di sini.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.formUrl != null) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            loadUrl(uiState.formUrl!!)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (uiState.showConfirmDialog) {
            ConfirmDialog(
                title = "Konfirmasi",
                message = "Apakah kamu yakin sudah menyelesaikan ujian?",
                onConfirm = {
                    viewModel.hideConfirmDialog()
                    viewModel.submitExam(sessionId)
                },
                onDismiss = { viewModel.hideConfirmDialog() }
            )
        }
    }
}
