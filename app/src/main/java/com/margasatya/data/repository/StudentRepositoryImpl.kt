package com.margasatya.data.repository

import com.margasatya.core.util.Resource
import com.margasatya.data.service.StudentAccessService
import com.margasatya.domain.model.Student
import com.margasatya.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val studentAccessService: StudentAccessService
) : StudentRepository {

    override suspend fun getStudentByNis(nis: String): Resource<Student> {
        return try {
            val student = studentAccessService.getStudentByNis(nis)
            if (student != null) {
                Resource.Success(student)
            } else {
                Resource.Error(
                    NoSuchElementException("Student not found"),
                    "Siswa dengan NIS $nis tidak ditemukan"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengambil data siswa")
        }
    }

    override suspend fun createStudent(student: Student): Resource<Student> {
        return try {
            val created = studentAccessService.createStudent(student)
            Resource.Success(created)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal menambahkan siswa")
        }
    }

    override suspend fun updateStudent(student: Student): Resource<Unit> {
        return try {
            studentAccessService.updateStudent(student)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengupdate siswa")
        }
    }

    override suspend fun deleteStudent(studentId: String): Resource<Unit> {
        return try {
            // Implement soft delete by setting isActive = false
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal menghapus siswa")
        }
    }

    override suspend fun getStudentsByTeacher(teacherId: String): Flow<Resource<List<Student>>> = flow {
        emit(Resource.Loading)
        try {
            val students = studentAccessService.listStudents(teacherId)
            emit(Resource.Success(students))
        } catch (e: Exception) {
            emit(Resource.Error(e, "Gagal mengambil daftar siswa"))
        }
    }
}
