package com.margasatya.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.margasatya.data.service.ExamService
import com.margasatya.domain.model.Exam
import com.margasatya.ui.components.LoadingDialog
import com.margasatya.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamListUiState(
    val exams: List<Exam> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TeacherExamListViewModel @Inject constructor(
    private val examService: ExamService,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamListUiState())
    val uiState: StateFlow<ExamListUiState> = _uiState.asStateFlow()

    fun loadExams() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val teacherId = firebaseAuth.currentUser?.uid ?: return@launch
                val exams = examService.listExams(teacherId)
                _uiState.value = _uiState.value.copy(
                    exams = exams,
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
fun TeacherExamListScreen(
    navController: NavController,
    viewModel: TeacherExamListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExams()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Ujian") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.TeacherExamForm.createRoute()) }
            ) {
                Icon(Icons.Default.Add, "Buat Ujian")
            }
        }
    ) { paddingValues ->
        if (uiState.exams.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Belum ada ujian. Buat ujian baru.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.exams) { exam ->
                    Card(
                        onClick = { navController.navigate(Screen.TeacherExamForm.createRoute(exam.id)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(exam.title, style = MaterialTheme.typography.titleMedium)
                            Text("Kode: ${exam.examCode}", style = MaterialTheme.typography.bodyMedium)
                            Text("Tipe: ${exam.type.name}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        }
    }
}
