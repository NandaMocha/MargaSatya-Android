package com.margasatya.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.margasatya.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beranda Guru") }
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
                text = "Selamat Datang",
                style = MaterialTheme.typography.headlineSmall
            )

            Card(
                onClick = { navController.navigate(Screen.TeacherStudentList.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kelola Siswa", style = MaterialTheme.typography.titleMedium)
                    Text("Tambah dan kelola data siswa", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Card(
                onClick = { navController.navigate(Screen.TeacherExamList.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kelola Ujian", style = MaterialTheme.typography.titleMedium)
                    Text("Buat dan kelola ujian", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
