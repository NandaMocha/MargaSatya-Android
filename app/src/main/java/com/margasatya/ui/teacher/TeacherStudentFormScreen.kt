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
import com.margasatya.data.service.StudentAccessService
import com.margasatya.domain.model.Student
import com.margasatya.ui.components.ErrorDialog
import com.margasatya.ui.components.LoadingDialog
import com.margasatya.ui.components.MargaSatyaTextField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentFormUiState(
    val nis: String = "",
    val name: String = "",
    val className: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TeacherStudentFormViewModel @Inject constructor(
    private val studentAccessService: StudentAccessService,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentFormUiState())
    val uiState: StateFlow<StudentFormUiState> = _uiState.asStateFlow()

    fun updateNis(nis: String) {
        _uiState.value = _uiState.value.copy(nis = nis)
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateClassName(className: String) {
        _uiState.value = _uiState.value.copy(className = className)
    }

    fun saveStudent() {
        viewModelScope.launch {
            val teacherId = firebaseAuth.currentUser?.uid ?: return@launch

            if (_uiState.value.nis.isEmpty() || _uiState.value.name.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "NIS dan Nama wajib diisi")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val student = Student(
                    teacherId = teacherId,
                    nis = _uiState.value.nis,
                    name = _uiState.value.name,
                    className = _uiState.value.className.ifEmpty { null }
                )

                studentAccessService.createStudent(student)
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal menyimpan siswa"
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
fun TeacherStudentFormScreen(
    navController: NavController,
    studentId: String,
    viewModel: TeacherStudentFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isNew = studentId == "new"

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Tambah Siswa" else "Edit Siswa") },
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
                value = uiState.nis,
                onValueChange = viewModel::updateNis,
                label = "NIS",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MargaSatyaTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = "Nama Siswa",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MargaSatyaTextField(
                value = uiState.className,
                onValueChange = viewModel::updateClassName,
                label = "Kelas (opsional)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveStudent,
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
