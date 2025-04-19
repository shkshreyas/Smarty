package com.shk.smarty.repository

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shk.smarty.model.Question
import com.shk.smarty.model.Quiz
import com.shk.smarty.model.Subject
import com.shk.smarty.model.Topic
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirebaseRepository"

@Singleton
class NewFirebaseRepository @Inject constructor() {
    // Initialize Firebase database
    private val database = Firebase.database
    
    // Base reference
    private val quizzesRef = database.reference.child("quizzes")
    
    // Child references
    private val subjectsRef = quizzesRef.child("subjects")
    
    // Get all subjects
    fun getSubjects(): Flow<List<Subject>> = callbackFlow {
        try {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val subjects = mutableListOf<Subject>()
                        
                        for (subjectSnapshot in snapshot.children) {
                            val id = subjectSnapshot.key ?: continue
                            val name = subjectSnapshot.child("name").getValue(String::class.java) ?: continue
                            val description = subjectSnapshot.child("description").getValue(String::class.java) ?: ""
                            val imageUrl = subjectSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                            
                            subjects.add(Subject(id, name, description, imageUrl))
                        }
                        
                        trySend(subjects)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing subjects: ${e.message}", e)
                        trySend(emptyList())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error: ${error.message}", error.toException())
                    trySend(emptyList())
                }
            }
            
            subjectsRef.addValueEventListener(listener)
            
            // Clean up listener when flow is cancelled
            awaitClose { subjectsRef.removeEventListener(listener) }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up subjects listener: ${e.message}", e)
            trySend(emptyList())
            awaitClose { }
        }
    }
    
    // Get topics for a subject
    fun getTopics(subjectId: String): Flow<List<Topic>> = callbackFlow {
        try {
            val topicsRef = subjectsRef.child(subjectId).child("topics")
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val topics = mutableListOf<Topic>()
                        
                        for (topicSnapshot in snapshot.children) {
                            val id = topicSnapshot.key ?: continue
                            val name = topicSnapshot.child("name").getValue(String::class.java) ?: continue
                            val description = topicSnapshot.child("description").getValue(String::class.java) ?: ""
                            val imageUrl = topicSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                            
                            topics.add(Topic(id, name, description, subjectId, imageUrl))
                        }
                        
                        trySend(topics)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing topics: ${e.message}", e)
                        trySend(emptyList())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error: ${error.message}", error.toException())
                    trySend(emptyList())
                }
            }
            
            topicsRef.addValueEventListener(listener)
            
            // Clean up listener when flow is cancelled
            awaitClose { topicsRef.removeEventListener(listener) }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up topics listener: ${e.message}", e)
            trySend(emptyList())
            awaitClose { }
        }
    }
    
    // Get quizzes for a topic
    fun getQuizzes(subjectId: String, topicId: String): Flow<List<Quiz>> = callbackFlow {
        try {
            val quizItemsRef = subjectsRef
                .child(subjectId)
                .child("topics")
                .child(topicId)
                .child("quizItems")
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val quizzes = mutableListOf<Quiz>()
                        
                        for (quizSnapshot in snapshot.children) {
                            val id = quizSnapshot.key ?: continue
                            val title = quizSnapshot.child("title").getValue(String::class.java) ?: continue
                            val description = quizSnapshot.child("description").getValue(String::class.java) ?: ""
                            val timeLimit = quizSnapshot.child("timeLimit").getValue(Int::class.java) ?: 10
                            val passingPercentage = quizSnapshot.child("passingPercentage").getValue(Int::class.java) ?: 70
                            
                            quizzes.add(Quiz(id, title, description, topicId, subjectId, timeLimit, passingPercentage))
                        }
                        
                        trySend(quizzes)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing quizzes: ${e.message}", e)
                        trySend(emptyList())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error: ${error.message}", error.toException())
                    trySend(emptyList())
                }
            }
            
            quizItemsRef.addValueEventListener(listener)
            
            // Clean up listener when flow is cancelled
            awaitClose { quizItemsRef.removeEventListener(listener) }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up quizzes listener: ${e.message}", e)
            trySend(emptyList())
            awaitClose { }
        }
    }
    
    // Get questions for a quiz
    fun getQuestions(subjectId: String, topicId: String, quizId: String): Flow<List<Question>> = callbackFlow {
        try {
            val questionsRef = subjectsRef
                .child(subjectId)
                .child("topics")
                .child(topicId)
                .child("quizItems")
                .child(quizId)
                .child("questions")
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val questions = mutableListOf<Question>()
                        
                        for (questionSnapshot in snapshot.children) {
                            val id = questionSnapshot.key ?: continue
                            val text = questionSnapshot.child("text").getValue(String::class.java) ?: continue
                            
                            // Get options
                            val optionsList = mutableListOf<String>()
                            val optionsSnapshot = questionSnapshot.child("options")
                            for (i in 0 until optionsSnapshot.childrenCount) {
                                val option = optionsSnapshot.child(i.toString()).getValue(String::class.java)
                                if (option != null) {
                                    optionsList.add(option)
                                }
                            }
                            
                            val correctOptionIndex = questionSnapshot.child("correctOptionIndex").getValue(Int::class.java) ?: 0
                            val explanation = questionSnapshot.child("explanation").getValue(String::class.java) ?: ""
                            
                            questions.add(Question(id, text, optionsList, correctOptionIndex, explanation, quizId))
                        }
                        
                        trySend(questions)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing questions: ${e.message}", e)
                        trySend(emptyList())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database error: ${error.message}", error.toException())
                    trySend(emptyList())
                }
            }
            
            questionsRef.addValueEventListener(listener)
            
            // Clean up listener when flow is cancelled
            awaitClose { questionsRef.removeEventListener(listener) }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up questions listener: ${e.message}", e)
            trySend(emptyList())
            awaitClose { }
        }
    }
    
    // Add a new subject
    suspend fun addSubject(subject: Subject): String {
        try {
            val newSubjectRef = subjectsRef.push()
            val subjectId = newSubjectRef.key ?: throw Exception("Failed to generate subject ID")
            
            val subjectData = mapOf(
                "name" to subject.name,
                "description" to subject.description,
                "imageUrl" to subject.imageUrl
            )
            
            newSubjectRef.setValue(subjectData).await()
            return subjectId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding subject: ${e.message}", e)
            throw e
        }
    }
    
    // Update an existing subject
    suspend fun updateSubject(subject: Subject) {
        try {
            val subjectRef = subjectsRef.child(subject.id)
            
            val subjectData = mapOf(
                "name" to subject.name,
                "description" to subject.description,
                "imageUrl" to subject.imageUrl
            )
            
            subjectRef.updateChildren(subjectData).await()
            Log.d(TAG, "Subject updated successfully: ${subject.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating subject: ${e.message}", e)
            throw e
        }
    }
    
    // Delete a subject
    suspend fun deleteSubject(subjectId: String) {
        try {
            val subjectRef = subjectsRef.child(subjectId)
            subjectRef.removeValue().await()
            Log.d(TAG, "Subject deleted successfully: $subjectId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subject: ${e.message}", e)
            throw e
        }
    }
    
    // Add a new topic to a subject
    suspend fun addTopic(topic: Topic): String {
        try {
            val topicsRef = subjectsRef.child(topic.subjectId).child("topics")
            val newTopicRef = topicsRef.push()
            val topicId = newTopicRef.key ?: throw Exception("Failed to generate topic ID")
            
            val topicData = mapOf(
                "name" to topic.name,
                "description" to topic.description,
                "imageUrl" to topic.imageUrl
            )
            
            newTopicRef.setValue(topicData).await()
            return topicId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding topic: ${e.message}", e)
            throw e
        }
    }
    
    // Add a new quiz to a topic
    suspend fun addQuiz(quiz: Quiz): String {
        try {
            val quizItemsRef = subjectsRef
                .child(quiz.subjectId)
                .child("topics")
                .child(quiz.topicId)
                .child("quizItems")
            
            val newQuizRef = quizItemsRef.push()
            val quizId = newQuizRef.key ?: throw Exception("Failed to generate quiz ID")
            
            val quizData = mapOf(
                "title" to quiz.title,
                "description" to quiz.description,
                "timeLimit" to quiz.timeLimit,
                "passingPercentage" to quiz.passingPercentage
            )
            
            newQuizRef.setValue(quizData).await()
            return quizId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding quiz: ${e.message}", e)
            throw e
        }
    }
    
    // Add a new question to a quiz
    suspend fun addQuestion(question: Question, subjectId: String, topicId: String): String {
        try {
            val questionsRef = subjectsRef
                .child(subjectId)
                .child("topics")
                .child(topicId)
                .child("quizItems")
                .child(question.quizId)
                .child("questions")
            
            val newQuestionRef = questionsRef.push()
            val questionId = newQuestionRef.key ?: throw Exception("Failed to generate question ID")
            
            val questionData = mapOf(
                "text" to question.text,
                "options" to question.options,
                "correctOptionIndex" to question.correctOptionIndex,
                "explanation" to question.explanation
            )
            
            newQuestionRef.setValue(questionData).await()
            return questionId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding question: ${e.message}", e)
            throw e
        }
    }
    
    // Import a quiz JSON
    suspend fun importQuizJson(quizJson: Map<String, Any>) {
        try {
            quizzesRef.updateChildren(quizJson).await()
            Log.d(TAG, "Quiz JSON imported successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error importing quiz JSON: ${e.message}", e)
            throw e
        }
    }
    
    // Migrate from old structure to new structure
    suspend fun migrateToNewStructure() {
        // This would be implemented to migrate existing data
        // Not implementing here as it would depend on the exact old structure
    }
    
    // Test connection and initialize if needed
    suspend fun testDatabaseConnection(): Result<Boolean> {
        return try {
            Log.d(TAG, "Testing database connection...")
            
            // First test basic connectivity using .info/connected
            val connectedRef = database.getReference(".info/connected")
            val snapshot = connectedRef.get().await()
            val isConnected = snapshot.getValue(Boolean::class.java) ?: false
            
            Log.d(TAG, "Database connection: $isConnected")
            
            if (isConnected) {
                // Check if structure exists
                val quizzesSnapshot = quizzesRef.child("subjects").get().await()
                if (!quizzesSnapshot.exists() || !quizzesSnapshot.hasChildren()) {
                    Log.d(TAG, "No quizzes found, adding sample data")
                    addSampleData()
                } else {
                    Log.d(TAG, "Found ${quizzesSnapshot.childrenCount} subjects")
                }
            }
            
            Result.success(isConnected)
        } catch (e: Exception) {
            Log.e(TAG, "Database connection test failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Add sample data for testing
    private suspend fun addSampleData() {
        try {
            // Create sample data structure
            val sampleData = mapOf(
                "quizzes" to mapOf(
                    "subjects" to mapOf(
                        "math" to mapOf(
                            "name" to "Mathematics",
                            "description" to "Learn about numbers, equations, geometry and more",
                            "imageUrl" to "https://example.com/math.jpg",
                            "topics" to mapOf(
                                "algebra" to mapOf(
                                    "name" to "Algebra",
                                    "description" to "Work with variables and equations",
                                    "quizItems" to mapOf(
                                        "quiz1" to mapOf(
                                            "title" to "Basic Equations",
                                            "description" to "Practice solving basic equations",
                                            "timeLimit" to 10,
                                            "passingPercentage" to 70,
                                            "questions" to mapOf(
                                                "q1" to mapOf(
                                                    "text" to "Solve for x: 2x + 3 = 7",
                                                    "options" to listOf("x = 2", "x = 3", "x = 4", "x = 5"),
                                                    "correctOptionIndex" to 0,
                                                    "explanation" to "Subtract 3 from both sides: 2x + 3 - 3 = 7 - 3, giving 2x = 4. Then divide both sides by 2: x = 2."
                                                ),
                                                "q2" to mapOf(
                                                    "text" to "Solve for y: 3y - 6 = 9",
                                                    "options" to listOf("y = 3", "y = 5", "y = 6", "y = 15"),
                                                    "correctOptionIndex" to 1,
                                                    "explanation" to "Add 6 to both sides: 3y - 6 + 6 = 9 + 6, giving 3y = 15. Then divide both sides by 3: y = 5."
                                                )
                                            )
                                        )
                                    )
                                ),
                                "geometry" to mapOf(
                                    "name" to "Geometry",
                                    "description" to "Study shapes and spatial relationships",
                                    "quizItems" to mapOf(
                                        "quiz1" to mapOf(
                                            "title" to "Basic Shapes",
                                            "description" to "Learn about triangles, circles, and more",
                                            "timeLimit" to 10,
                                            "passingPercentage" to 70,
                                            "questions" to mapOf(
                                                "q1" to mapOf(
                                                    "text" to "What is the formula for the area of a circle?",
                                                    "options" to listOf("πr²", "2πr", "πd", "r²π"),
                                                    "correctOptionIndex" to 0,
                                                    "explanation" to "The area of a circle is calculated using the formula A = πr², where r is the radius of the circle."
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        "science" to mapOf(
                            "name" to "Science",
                            "description" to "Explore the natural world through physics, chemistry, and biology",
                            "imageUrl" to "https://example.com/science.jpg",
                            "topics" to mapOf(
                                "physics" to mapOf(
                                    "name" to "Physics",
                                    "description" to "Study matter, energy, and forces",
                                    "quizItems" to mapOf(
                                        "quiz1" to mapOf(
                                            "title" to "Newton's Laws",
                                            "description" to "Test your understanding of Newton's Laws of Motion",
                                            "timeLimit" to 15,
                                            "passingPercentage" to 70,
                                            "questions" to mapOf(
                                                "q1" to mapOf(
                                                    "text" to "Which of Newton's laws states that an object at rest remains at rest unless acted upon by a force?",
                                                    "options" to listOf("First Law", "Second Law", "Third Law", "Fourth Law"),
                                                    "correctOptionIndex" to 0,
                                                    "explanation" to "Newton's First Law of Motion states that an object at rest stays at rest and an object in motion stays in motion with the same speed and in the same direction unless acted upon by an unbalanced force."
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
            
            // Import sample data
            database.reference.updateChildren(sampleData as Map<String, Any>).await()
            Log.d(TAG, "Sample data added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding sample data: ${e.message}", e)
            throw e
        }
    }
} 