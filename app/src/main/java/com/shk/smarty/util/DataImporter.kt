package com.shk.smarty.util

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Utility for importing data directly into Firebase
 */
object DataImporter {
    private const val TAG = "DataImporter"
    
    /**
     * Import the history quiz data directly to Firebase
     */
    suspend fun importHistoryQuizData(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val database = Firebase.database
            val rootRef = database.reference
            
            // Define the history quiz data
            val historyData = mapOf(
                "quizzes" to mapOf(
                    "subjects" to mapOf(
                        "history" to mapOf(
                            "name" to "History",
                            "description" to "Explore world history",
                            "imageUrl" to "https://example.com/history.jpg",
                            "topics" to mapOf(
                                "ancient" to mapOf(
                                    "name" to "Ancient History",
                                    "description" to "Study ancient civilizations",
                                    "quizItems" to mapOf(
                                        "quiz1" to mapOf(
                                            "title" to "Ancient Egypt",
                                            "description" to "Test your knowledge of Ancient Egypt",
                                            "timeLimit" to 15,
                                            "passingPercentage" to 70,
                                            "questions" to mapOf(
                                                "q1" to mapOf(
                                                    "text" to "Who was the famous boy-king of Egypt?",
                                                    "options" to listOf(
                                                        "Ramses II",
                                                        "Tutankhamun",
                                                        "Cleopatra",
                                                        "Akhenaten"
                                                    ),
                                                    "correctOptionIndex" to 1,
                                                    "explanation" to "Tutankhamun, often referred to as King Tut, was an Egyptian pharaoh who ruled from 1332–1323 BCE."
                                                ),
                                                "q2" to mapOf(
                                                    "text" to "Which ancient wonder was located in Egypt?",
                                                    "options" to listOf(
                                                        "Temple of Artemis",
                                                        "Colossus of Rhodes",
                                                        "Great Pyramid of Giza",
                                                        "Hanging Gardens of Babylon"
                                                    ),
                                                    "correctOptionIndex" to 2,
                                                    "explanation" to "The Great Pyramid of Giza is the only surviving ancient wonder."
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
            
            // Update the database
            rootRef.updateChildren(historyData as Map<String, Any>).await()
            
            Log.d(TAG, "History quiz data imported successfully")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing history quiz data: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Test Firebase connection
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val database = Firebase.database
            val connectedRef = database.getReference(".info/connected")
            val snapshot = connectedRef.get().await()
            val isConnected = snapshot.getValue(Boolean::class.java) ?: false
            
            Log.d(TAG, "Firebase connection: $isConnected")
            
            if (isConnected) {
                // Check if data exists
                val subjectsRef = database.getReference("quizzes/subjects")
                val subjectsSnapshot = subjectsRef.get().await()
                
                if (!subjectsSnapshot.exists() || !subjectsSnapshot.childrenCount.equals(0)) {
                    Log.d(TAG, "No subjects found in database")
                } else {
                    Log.d(TAG, "Found ${subjectsSnapshot.childrenCount} subjects")
                }
            }
            
            Result.success(isConnected)
        } catch (e: Exception) {
            Log.e(TAG, "Error testing Firebase connection: ${e.message}", e)
            Result.failure(e)
        }
    }
} 