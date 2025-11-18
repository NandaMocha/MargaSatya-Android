package com.margasatya.ui.teacher

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
import com.google.firebase.auth.FirebaseAuth
import com.margasatya.data.service.ExamService
import com.margasatya.domain.enums.ExamType
import com.margasatya.domain.model.ExamDraft
import com.margasatya.ui.components.ErrorDialog
import com.margasatya.ui.components.LoadingDialog
import com.margasatya.ui.components.MargaSatyaTextField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamFormUiState(
    val title: String = "",
    val description: String = "",
    val examCode: String = "",
    val examType: ExamType = ExamType.IN_APP,
    val formUrl: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TeacherExamFormViewModel @Inject constructor(
    private val examService: ExamService,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamFormUiState())
    val uiState: StateFlow<ExamFormUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateExamCode(examCode: String) {
        _uiState.value = _uiState.value.copy(examCode = examCode)
    }

    fun updateExamType(examType: ExamType) {
        _uiState.value = _uiState.value.copy(examType = examType)
    }

    fun updateFormUrl(formUrl: String) {
        _uiState.value = _uiState.value.copy(formUrl = formUrl)
    }

    fun saveExam() {
        viewModelScope.launch {
            val teacherId = firebaseAuth.currentUser?.uid ?: return@launch

            if (_uiState.value.title.isEmpty() || _uiState.value.examCode.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Nama ujian dan kode ujian wajib diisi")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val draft = ExamDraft(
                    teacherId = teacherId,
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    examCode = _uiState.value.examCode,
                    type = _uiState.value.examType,
                    formUrl = if (_uiState.value.examType == ExamType.GOOGLE_FORM) _uiState.value.formUrl else null
                )

                examService.createExam(draft, teacherId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal menyimpan ujian"
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
fun TeacherExamFormScreen(
    navController: NavController,
    examId: String,
    viewModel: TeacherExamFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isNew = examId == "new"

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Buat Ujian Baru" else "Edit Ujian") },
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
                .padding(24.dp)
        ) {
            MargaSatyaTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = "Nama Ujian",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MargaSatyaTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = "Deskripsi",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MargaSatyaTextField(
                value = uiState.examCode,
                onValueChange = viewModel::updateExamCode,
                label = "Kode Ujian",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tipe Ujian", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.examType == ExamType.IN_APP,
                    onClick = { viewModel.updateExamType(ExamType.IN_APP) },
                    label = { Text("In-App") }
                )
                FilterChip(
                    selected = uiState.examType == ExamType.GOOGLE_FORM,
                    onClick = { viewModel.updateExamType(ExamType.GOOGLE_FORM) },
                    label = { Text("Google Form") }
                )
            }

            if (uiState.examType == ExamType.GOOGLE_FORM) {
                Spacer(modifier = Modifier.height(16.dp))
                MargaSatyaTextField(
                    value = uiState.formUrl,
                    onValueChange = viewModel::updateFormUrl,
                    label = "URL Google Form",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveExam,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                Text("Simpan")
            }
        }

        if (uiState.isLoading) {
            LoadingDialog()
        }

        if (uiState.error != null) {
            ErrorDialog(
                message = uiState.error!!,
                onDismiss = viewModel::clearError
            )
        }
    }
}
