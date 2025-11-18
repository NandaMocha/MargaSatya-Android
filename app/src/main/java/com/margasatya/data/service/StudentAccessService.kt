package com.margasatya.data.service

import com.margasatya.domain.model.Student

interface StudentAccessService {
    suspend fun getStudentByNis(nis: String, teacherId: String? = null): Student?
    suspend fun createStudent(student: Student): Student
    suspend fun updateStudent(student: Student)
    suspend fun listStudents(teacherId: String): List<Student>
    suspend fun isStudentAllowed(nis: String, examId: String): Boolean
}
