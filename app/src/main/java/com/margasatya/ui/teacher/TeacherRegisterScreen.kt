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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.domain.enums.UserRole
import com.margasatya.ui.components.ErrorDialog
import com.margasatya.ui.components.LoadingDialog
import com.margasatya.ui.components.MargaSatyaTextField
import com.margasatya.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class TeacherRegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false
)

@HiltViewModel
class TeacherRegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherRegisterUiState())
    val uiState: StateFlow<TeacherRegisterUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun register() {
        viewModelScope.launch {
            val name = _uiState.value.name.trim()
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password
            val confirmPassword = _uiState.value.confirmPassword

            // Validate
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Semua field harus diisi")
                return@launch
            }

            if (password.length < 6) {
                _uiState.value = _uiState.value.copy(error = "Password minimal 6 karakter")
                return@launch
            }

            if (password != confirmPassword) {
                _uiState.value = _uiState.value.copy(error = "Password dan konfirmasi password tidak sama")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Create user
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: throw Exception("Failed to get user ID")

                // Create user profile in Firestore
                val now = Timestamp.now()
                val userData = mapOf(
                    "id" to userId,
                    "name" to name,
                    "email" to email,
                    "role" to UserRole.GURU.name,
                    "createdAt" to now,
                    "updatedAt" to now
                )

                firestore.collection("users")
                    .document(userId)
                    .set(userData)
                    .await()

                _uiState.value = _uiState.value.copy(isLoading = false, isRegistered = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Registrasi gagal. Silakan coba lagi."
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
fun TeacherRegisterScreen(
    navController: NavController,
    viewModel: TeacherRegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            navController.navigate(Screen.TeacherHome.route) {
                popUpTo(Screen.RoleSelection.route) { inclusive = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Guru") },
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
            Text(
                text = "Buat Akun Guru",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            MargaSatyaTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = "Nama Lengkap",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MargaSatyaTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                label = "Email",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MargaSatyaTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = "Password",
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MargaSatyaTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                label = "Konfirmasi Password",
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::register,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                Text("Daftar")
            }
        }

        if (uiState.isLoading) {
            LoadingDialog(message = "Membuat akun...")
        }

        if (uiState.error != null) {
            ErrorDialog(
                message = uiState.error!!,
                onDismiss = viewModel::clearError
            )
        }
    }
}
