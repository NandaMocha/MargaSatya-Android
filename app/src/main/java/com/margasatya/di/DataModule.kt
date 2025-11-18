package com.margasatya.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.data.repository.*
import com.margasatya.data.service.*
import com.margasatya.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Data Module following Dependency Inversion Principle
 * Provides repository implementations, not services directly
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // Services (internal to data layer)
    @Provides
    @Singleton
    fun provideExamService(
        firestore: FirebaseFirestore
    ): ExamService {
        return FirestoreExamService(firestore)
    }

    @Provides
    @Singleton
    fun provideStudentAccessService(
        firestore: FirebaseFirestore
    ): StudentAccessService {
        return FirestoreStudentAccessService(firestore)
    }

    @Provides
    @Singleton
    fun provideExamSessionService(
        firestore: FirebaseFirestore
    ): ExamSessionService {
        return FirestoreExamSessionService(firestore)
    }

    @Provides
    @Singleton
    fun provideExamAnswerService(
        firestore: FirebaseFirestore
    ): ExamAnswerService {
        return FirestoreExamAnswerService(firestore)
    }

    @Provides
    @Singleton
    fun provideAdminStatisticsService(
        firestore: FirebaseFirestore
    ): AdminStatisticsService {
        return FirestoreAdminStatisticsService(firestore)
    }

    // Repositories (exposed to domain layer)
    @Provides
    @Singleton
    fun provideExamRepository(
        examService: ExamService
    ): ExamRepository {
        return ExamRepositoryImpl(examService)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideStudentRepository(
        studentService: StudentAccessService
    ): StudentRepository {
        return StudentRepositoryImpl(studentService)
    }

    @Provides
    @Singleton
    fun provideExamSessionRepository(
        sessionService: ExamSessionService
    ): ExamSessionRepository {
        return ExamSessionRepositoryImpl(sessionService)
    }

    @Provides
    @Singleton
    fun provideAnswerRepository(
        answerService: ExamAnswerService
    ): AnswerRepository {
        return AnswerRepositoryImpl(answerService)
    }
}
