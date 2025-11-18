package com.margasatya.core.lock

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

interface ExamLockManager {
    fun startExamLock(activity: Activity): Boolean
    fun stopExamLock(activity: Activity)
    fun isInLockMode(): Boolean
}

class ExamLockManagerImpl(
    private val context: Context
) : ExamLockManager {

    private var isLocked = false
    private val tag = "ExamLockManager"

    override fun startExamLock(activity: Activity): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

                // Check if device owner or lock task is permitted
                if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                    // Try to start lock task mode
                    activity.startLockTask()
                    isLocked = true
                    Log.d(tag, "Lock task mode started successfully")
                    true
                } else {
                    Log.w(tag, "Already in lock task mode")
                    isLocked = true
                    true
                }
            } else {
                // For older versions, we can't use lock task mode
                Log.w(tag, "Lock task mode not supported on this Android version")
                false
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start lock task mode", e)
            isLocked = false
            false
        }
    }

    override fun stopExamLock(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isLocked) {
                activity.stopLockTask()
                isLocked = false
                Log.d(tag, "Lock task mode stopped")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to stop lock task mode", e)
        }
    }

    override fun isInLockMode(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        }
        return isLocked
    }
}
