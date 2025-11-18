package com.margasatya.data.service

import com.margasatya.domain.model.AdminSummary
import com.margasatya.domain.model.Exam

interface AdminStatisticsService {
    suspend fun getSummary(): AdminSummary
    suspend fun getRecentExams(limit: Int): List<Exam>
}
