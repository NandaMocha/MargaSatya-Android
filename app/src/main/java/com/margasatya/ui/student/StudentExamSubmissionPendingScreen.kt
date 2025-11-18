package com.margasatya.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.margasatya.core.network.NetworkMonitor
import com.margasatya.data.service.ExamAnswerService
import com.margasatya.data.service.ExamSessionService
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.ui.components.ErrorDialog
import com.margasatya.ui.components.LoadingDialog
import com.margasatya.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubmissionPendingUiState(
    val isRetrying: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StudentExamSubmissionPendingViewModel @Inject constructor(
    private val examSessionService: ExamSessionService,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmissionPendingUiState())
    val uiState: StateFlow<SubmissionPendingUiState> = _uiState.asStateFlow()

    fun retrySubmit(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRetrying = true, error = null)

            try {
                // Check network
                if (!networkMonitor.isCurrentlyConnected()) {
                    _uiState.value = _uiState.value.copy(
                        isRetrying = false,
                        error = "Koneksi internet tidak tersedia. Silakan periksa koneksi Anda."
                    )
                    return@launch
                }

                // Try to update status to SUBMITTED
                examSessionService.updateSessionStatus(sessionId, ExamSessionStatus.SUBMITTED)

                _uiState.value = _uiState.value.copy(
                    isRetrying = false,
                    isSubmitted = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRetrying = false,
                    error = "Gagal mengirim jawaban. Silakan coba lagi."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentExamSubmissionPendingScreen(
    navController: NavController,
    sessionId: String,
    viewModel: StudentExamSubmissionPendingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            navController.navigate(Screen.RoleSelection.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengiriman Ujian Tertunda") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.warning
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pengiriman Ujian Tertunda",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Jawabanmu tersimpan di perangkat, namun belum berhasil dikirim karena koneksi internet. Setelah koneksi stabil, tekan tombol 'Kirim Ulang'.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.retrySubmit(sessionId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isRetrying
            ) {
                Text("Cek Koneksi & Kirim Ulang")
            }
        }

        if (uiState.isRetrying) {
            LoadingDialog(message = "Mengirim jawaban...")
        }

        if (uiState.error != null) {
            ErrorDialog(
                message = uiState.error!!,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}
