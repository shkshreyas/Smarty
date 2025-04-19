package com.shk.smarty.repository

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shk.smarty.model.Module
import com.shk.smarty.model.Question
import com.shk.smarty.model.Quiz
import com.shk.smarty.model.Subject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor() {
    private val database = FirebaseDatabase.getInstance()
    
    // References
    private val subjectsRef = database.reference.child("subjects")
    private val modulesRef = database.reference.child("modules")
    private val quizzesRef = database.reference.child("quizzes")
    private val questionsRef = database.reference.child("questions")
    
    // Fetch all subjects
    fun getSubjects(): Flow<List<Subject>> = callbackFlow {
        try {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val subjects = snapshot.children.mapNotNull { it.getValue(Subject::class.java) }
                        trySend(subjects)
                    } catch (e: Exception) {
                        Log.e("FirebaseRepository", "Error parsing subjects: ${e.message}", e)
                        trySend(emptyList<Subject>())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseRepository", "Database error: ${error.message}", error.toException())
                    trySend(emptyList<Subject>())
                }
            }
            
            subjectsRef.addValueEventListener(listener)
            
            // Clean up listener when flow is cancelled
            awaitClose { subjectsRef.removeEventListener(listener) }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error setting up subjects listener: ${e.message}", e)
            trySend(emptyList<Subject>())
            awaitClose { }
        }
    }
    
    // Fetch modules by subject ID
    fun getModulesBySubject(subjectId: String): Flow<List<Module>> = callbackFlow {
        try {
            val query = modulesRef.orderByChild("subjectId").equalTo(subjectId)
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val modules = snapshot.children.mapNotNull { snapshot ->
                            snapshot.getValue(Module::class.java)
                        }
                        trySend(modules)
                    } catch (e: Exception) {
                        Log.e("FirebaseRepository", "Error parsing modules: ${e.message}", e)
                        trySend(emptyList<Module>())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseRepository", "Database error: ${error.message}", error.toException())
                    trySend(emptyList<Module>())
                }
            }
            
            query.addValueEventListener(listener)
            
            // Clean up listener when flow is cancelled
            awaitClose { query.removeEventListener(listener) }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error setting up modules listener: ${e.message}", e)
            trySend(emptyList<Module>())
            awaitClose { }
        }
    }
    
    // Fetch quizzes by module ID
    fun getQuizzesByModule(moduleId: String): Flow<List<Quiz>> = callbackFlow {
        try {
            val query = quizzesRef.orderByChild("moduleId").equalTo(moduleId)
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val quizzes = snapshot.children.mapNotNull { it.getValue(Quiz::class.java) }
                        trySend(quizzes)
                    } catch (e: Exception) {
                        Log.e("FirebaseRepository", "Error parsing quizzes: ${e.message}", e)
                        trySend(emptyList<Quiz>())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseRepository", "Database error fetching quizzes: ${error.message}", error.toException())
                    trySend(emptyList<Quiz>())
                }
            }
            
            query.addValueEventListener(listener)
            
            // Clean up listener when flow is cancelled
            awaitClose { query.removeEventListener(listener) }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error setting up quizzes listener: ${e.message}", e)
            trySend(emptyList<Quiz>())
            awaitClose { }
        }
    }
    
    // Fetch questions by quiz ID
    fun getQuestionsByQuiz(quizId: String): Flow<List<Question>> = callbackFlow {
        try {
            val query = questionsRef.orderByChild("quizId").equalTo(quizId)
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val questions = snapshot.children.mapNotNull { it.getValue(Question::class.java) }
                        trySend(questions)
                    } catch (e: Exception) {
                        Log.e("FirebaseRepository", "Error parsing questions: ${e.message}", e)
                        trySend(emptyList<Question>())
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseRepository", "Database error fetching questions: ${error.message}", error.toException())
                    trySend(emptyList<Question>())
                }
            }
            
            query.addValueEventListener(listener)
            
            // Clean up listener when flow is cancelled
            awaitClose { query.removeEventListener(listener) }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error setting up questions listener: ${e.message}", e)
            trySend(emptyList<Question>())
            awaitClose { }
        }
    }
    
    // Add a new subject
    suspend fun addSubject(subject: Subject): String {
        val newSubjectRef = subjectsRef.push()
        val subjectWithId = subject.copy(id = newSubjectRef.key ?: "")
        newSubjectRef.setValue(subjectWithId).await()
        return subjectWithId.id
    }
    
    // Add a new module
    suspend fun addModule(module: Module): String {
        val newModuleRef = modulesRef.push()
        val moduleWithId = module.copy(id = newModuleRef.key ?: "")
        newModuleRef.setValue(moduleWithId).await()
        return moduleWithId.id
    }
    
    // Add a new quiz
    suspend fun addQuiz(quiz: Quiz): String {
        val newQuizRef = quizzesRef.push()
        val quizWithId = quiz.copy(id = newQuizRef.key ?: "")
        newQuizRef.setValue(quizWithId).await()
        return quizWithId.id
    }
    
    // Add a new question
    suspend fun addQuestion(question: Question): String {
        val newQuestionRef = questionsRef.push()
        val questionWithId = question.copy(id = newQuestionRef.key ?: "")
        newQuestionRef.setValue(questionWithId).await()
        return questionWithId.id
    }
    
    // Update a subject
    suspend fun updateSubject(subject: Subject) {
        subjectsRef.child(subject.id).setValue(subject).await()
    }
    
    // Update a module
    suspend fun updateModule(module: Module) {
        modulesRef.child(module.id).setValue(module).await()
    }
    
    // Update a quiz
    suspend fun updateQuiz(quiz: Quiz) {
        quizzesRef.child(quiz.id).setValue(quiz).await()
    }
    
    // Update a question
    suspend fun updateQuestion(question: Question) {
        questionsRef.child(question.id).setValue(question).await()
    }
    
    // Delete a subject and all related modules, quizzes, and questions
    suspend fun deleteSubject(subjectId: String) {
        // First, get all modules for this subject
        val moduleSnapshot = modulesRef
            .orderByChild("subjectId")
            .equalTo(subjectId)
            .get()
            .await()
            
        // Delete all modules and their quizzes
        for (moduleSnap in moduleSnapshot.children) {
            val module = moduleSnap.getValue(Module::class.java)
            module?.let { deleteModule(it.id) }
        }
        
        // Finally delete the subject
        subjectsRef.child(subjectId).removeValue().await()
    }
    
    // Delete a module and all related quizzes and questions
    suspend fun deleteModule(moduleId: String) {
        // First, get all quizzes for this module
        val quizSnapshot = quizzesRef
            .orderByChild("moduleId")
            .equalTo(moduleId)
            .get()
            .await()
            
        // Delete all quizzes and their questions
        for (quizSnap in quizSnapshot.children) {
            val quiz = quizSnap.getValue(Quiz::class.java)
            quiz?.let { deleteQuiz(it.id) }
        }
        
        // Finally delete the module
        modulesRef.child(moduleId).removeValue().await()
    }
    
    // Delete a quiz and all related questions
    suspend fun deleteQuiz(quizId: String) {
        // First, delete all questions for this quiz
        val questionSnapshot = questionsRef
            .orderByChild("quizId")
            .equalTo(quizId)
            .get()
            .await()
            
        for (questionSnap in questionSnapshot.children) {
            questionSnap.ref.removeValue().await()
        }
        
        // Then delete the quiz
        quizzesRef.child(quizId).removeValue().await()
    }
    
    // Test connection and add sample subjects if empty
    suspend fun testDatabaseConnection(): Result<Boolean> {
        return try {
            Log.d("FirebaseRepository", "Testing database connection...")
            
            // First test basic connectivity using .info/connected
            val connectedRef = database.getReference(".info/connected")
            val snapshot = connectedRef.get().await()
            val isConnected = snapshot.getValue(Boolean::class.java) ?: false
            
            Log.d("FirebaseRepository", "Database connection: $isConnected")
            
            if (isConnected) {
                // If connected, check if subjects exist
                try {
                    val subjectsSnapshot = subjectsRef.get().await()
                    if (!subjectsSnapshot.exists() || !subjectsSnapshot.hasChildren()) {
                        Log.d("FirebaseRepository", "No subjects found, adding sample data")
                        addSampleData()
                    } else {
                        Log.d("FirebaseRepository", "Found ${subjectsSnapshot.childrenCount} subjects")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseRepository", "Error checking subjects: ${e.message}", e)
                    // Don't fail the connection test just because we couldn't check subjects
                }
            }
            
            Result.success(isConnected)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Database connection test failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Add sample data for testing
    private suspend fun addSampleData() {
        try {
            // Add subjects
            val mathSubject = Subject(
                id = "",
                name = "Mathematics",
                description = "Learn about numbers, equations, geometry and more",
                imageUrl = "https://example.com/math.jpg"
            )
            val mathId = addSubject(mathSubject)
            
            val scienceSubject = Subject(
                id = "",
                name = "Science",
                description = "Explore physics, chemistry, biology and other sciences",
                imageUrl = "https://example.com/science.jpg"
            )
            val scienceId = addSubject(scienceSubject)
            
            // Add modules
            val algebraModule = Module(
                id = "",
                title = "Algebra",
                description = "Solve equations and work with variables",
                subjectId = mathId
            )
            val algebraId = addModule(algebraModule)
            
            val geometryModule = Module(
                id = "",
                title = "Geometry",
                description = "Study shapes, sizes, and properties of space",
                subjectId = mathId
            )
            val geometryId = addModule(geometryModule)
            
            // Add quizzes
            val basicAlgebraQuiz = Quiz(
                id = "",
                title = "Basic Algebra",
                description = "Test your knowledge of basic algebraic concepts",
                topicId = "",
                subjectId = "",
                timeLimit = 15,
                passingPercentage = 70
            )
            addQuiz(basicAlgebraQuiz)
            
            Log.d("FirebaseRepository", "Sample data added successfully")
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error adding sample data: ${e.message}", e)
        }
    }
}