package com.marshall.pyerite.sdeModule.update

import android.util.Log

object SdeUpdateLog {
    const val TAG = "SdeUpdate"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun d(component: String, message: String) {
        Log.d(TAG, "[$component] $message")
    }

    fun w(component: String, message: String, error: Throwable? = null) {
        if (error != null) {
            Log.w(TAG, "[$component] $message", error)
        } else {
            Log.w(TAG, "[$component] $message")
        }
    }
}
