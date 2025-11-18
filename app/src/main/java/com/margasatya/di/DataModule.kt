package com.margasatya.di

import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.data.repository.*
import com.margasatya.data.service.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

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
}
