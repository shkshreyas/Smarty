package com.shk.smarty.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Utility class for importing quiz JSON data
 */
object JsonImporter {
    private const val TAG = "JsonImporter"
    
    /**
     * Import quiz JSON from a file URI
     *
     * @param context Application context
     * @param fileUri The URI of the JSON file to import
     * @return Result indicating success or failure
     */
    suspend fun importQuizJson(context: Context, fileUri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Read file content
            val jsonString = readJsonFromUri(context, fileUri)
            
            // Parse JSON
            val jsonObject = JSONObject(jsonString)
            
            // Convert to Map
            val jsonMap = convertJsonToMap(jsonObject)
            
            // Update Firebase database
            val database = FirebaseDatabase.getInstance()
            database.reference.updateChildren(jsonMap).await()
            
            Log.d(TAG, "Quiz JSON imported successfully")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing quiz JSON: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Read JSON content from a URI
     */
    @Throws(IOException::class)
    private fun readJsonFromUri(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                return stringBuilder.toString()
            }
        } ?: throw IOException("Could not open input stream")
    }
    
    /**
     * Convert JSONObject to Map<String, Any>
     */
    @Throws(JSONException::class)
    private fun convertJsonToMap(jsonObject: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        
        jsonObject.keys().forEach { key ->
            val value = jsonObject.get(key)
            
            map[key] = when (value) {
                is JSONObject -> convertJsonToMap(value)
                is org.json.JSONArray -> {
                    val list = mutableListOf<Any>()
                    for (i in 0 until value.length()) {
                        val item = value.get(i)
                        when (item) {
                            is JSONObject -> list.add(convertJsonToMap(item))
                            else -> list.add(item)
                        }
                    }
                    list
                }
                else -> value
            }
        }
        
        return map
    }
    
    /**
     * Generate a sample quiz JSON string
     */
    fun generateSampleQuizJson(): String {
        val sampleData = mapOf(
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
                                                "explanation" to "Tutankhamun, often referred to as King Tut, was an Egyptian pharaoh who ruled from 1332-1323 BCE."
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
        
        return Gson().toJson(sampleData)
    }
} 