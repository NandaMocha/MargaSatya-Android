package com.margasatya.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.margasatya.data.service.AdminStatisticsService
import com.margasatya.domain.model.AdminSummary
import com.margasatya.ui.components.LoadingDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val summary: AdminSummary? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminStatisticsService: AdminStatisticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val summary = adminStatisticsService.getSummary()
                _uiState.value = _uiState.value.copy(
                    summary = summary,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSummary()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Admin") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Statistik Aplikasi",
                style = MaterialTheme.typography.headlineSmall
            )

            if (uiState.summary != null) {
                StatCard("Total Guru", uiState.summary!!.totalTeachers.toString())
                StatCard("Total Siswa", uiState.summary!!.totalStudents.toString())
                StatCard("Total Ujian", uiState.summary!!.totalExams.toString())
                StatCard("Ujian Berjalan", uiState.summary!!.runningExams.toString())
                StatCard("Ujian Selesai", uiState.summary!!.finishedExams.toString())
                StatCard("Sesi Hari Ini", uiState.summary!!.sessionsToday.toString())
            }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
