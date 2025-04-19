package com.shk.smarty

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartyApplication : Application() {
    
    companion object {
        private const val TAG = "SmartyApplication"
        private const val DATABASE_URL = "https://smarty-party-a5ee0-default-rtdb.firebaseio.com"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // Configure Firebase Database with explicit URL
            try {
                FirebaseDatabase.getInstance().apply {
                    // Enable persistence for offline capabilities
                    setPersistenceEnabled(true)
                    // Keep data synced
                    reference.keepSynced(true)
                }
                Log.d(TAG, "Firebase initialized successfully")
            } catch (e: Exception) {
                // This can happen if persistence is already enabled
                Log.w(TAG, "Firebase persistence already enabled: ${e.message}")
                FirebaseDatabase.getInstance().reference.keepSynced(true)
                Log.d(TAG, "Firebase initialization completed with default settings")
            }
            
            // Also initialize with the explicit URL as a backup approach
            try {
                Firebase.database.setPersistenceEnabled(true)
            } catch (e: Exception) {
                Log.w(TAG, "Firebase persistence already enabled: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}", e)
        }
    }
}