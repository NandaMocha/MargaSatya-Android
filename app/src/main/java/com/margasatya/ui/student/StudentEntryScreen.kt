package com.margasatya.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.margasatya.R
import com.margasatya.domain.enums.ExamType
import com.margasatya.ui.components.ErrorDialog
import com.margasatya.ui.components.LoadingDialog
import com.margasatya.ui.components.MargaSatyaTextField
import com.margasatya.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEntryScreen(
    navController: NavController,
    viewModel: StudentEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToExam) {
        if (uiState.navigateToExam && uiState.exam != null && uiState.session != null) {
            val route = when (uiState.exam!!.type) {
                ExamType.GOOGLE_FORM -> Screen.StudentExamWebView.createRoute(
                    uiState.exam!!.id,
                    uiState.session!!.id
                )
                ExamType.IN_APP -> Screen.StudentExamInApp.createRoute(
                    uiState.exam!!.id,
                    uiState.session!!.id
                )
            }
            navController.navigate(route) {
                popUpTo(Screen.StudentEntry.route) { inclusive = true }
            }
            viewModel.clearNavigation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Masuk Ujian") },
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
                text = "Masukkan NIS dan Kode Ujian",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            MargaSatyaTextField(
                value = uiState.nis,
                onValueChange = viewModel::updateNis,
                label = stringResource(R.string.label_nis),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            MargaSatyaTextField(
                value = uiState.examCode,
                onValueChange = viewModel::updateExamCode,
                label = stringResource(R.string.label_exam_code),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::startExam,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                Text(stringResource(R.string.btn_start_exam))
            }
        }

        if (uiState.isLoading) {
            LoadingDialog(message = "Memuat ujian...")
        }

        if (uiState.error != null) {
            ErrorDialog(
                message = uiState.error!!,
                onDismiss = viewModel::clearError
            )
        }
    }
}
