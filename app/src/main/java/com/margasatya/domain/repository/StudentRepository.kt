package com.margasatya.domain.repository

import com.margasatya.core.util.Resource
import com.margasatya.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    suspend fun getStudentByNis(nis: String): Resource<Student>
    suspend fun createStudent(student: Student): Resource<Student>
    suspend fun updateStudent(student: Student): Resource<Unit>
    suspend fun deleteStudent(studentId: String): Resource<Unit>
    suspend fun getStudentsByTeacher(teacherId: String): Flow<Resource<List<Student>>>
}
