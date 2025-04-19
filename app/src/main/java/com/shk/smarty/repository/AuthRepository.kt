package com.shk.smarty.repository

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton
import com.google.android.gms.common.api.CommonStatusCodes

private const val TAG = "AuthRepository"

// Define GoogleSignInStatusCodes constants since they might not be directly available
private object GoogleSignInStatusCodes {
    const val SIGN_IN_CANCELLED = 12501
    const val SIGN_IN_FAILED = 12500
    const val SIGN_IN_CURRENTLY_IN_PROGRESS = 12502
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private val usersRef = database.getReference("users")
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    // Email authentication methods
    suspend fun signUpWithEmail(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { user ->
                // Create user profile in database
                createUserProfile(user.uid, username, email, user.photoUrl?.toString())
                Result.success(user)
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sign up error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "Starting sign in attempt with email: ${email}")
            
            // Test database connection first
            try {
                database.reference.child(".info").child("connected").get().await()
                Log.d(TAG, "Firebase database connection successful")
            } catch (e: Exception) {
                Log.e(TAG, "Firebase database connection failed: ${e.message}", e)
            }
            
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let { user ->
                Log.d(TAG, "Sign in successful")
                Result.success(user)
            } ?: Result.failure(Exception("Sign in failed - no user returned"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sign in error: ${e.message}", e)
            if (e.message?.contains("network") == true || e.message?.contains("timeout") == true) {
                Result.failure(Exception("Network error: Check your internet connection"))
            } else {
                Result.failure(e)
            }
        }
    }
    
    // Google Sign In methods
    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            authResult.user?.let { user ->
                // If this is a new user (first sign in), create their profile
                if (authResult.additionalUserInfo?.isNewUser == true) {
                    account.displayName?.let { displayName ->
                        createUserProfile(user.uid, displayName, account.email ?: "", account.photoUrl?.toString())
                    }
                }
                Result.success(user)
            } ?: Result.failure(Exception("Google sign in failed"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Google auth error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun handleSignInResult(data: Intent?): Result<GoogleSignInAccount> {
        return try {
            if (data == null) {
                Log.e(TAG, "Google sign in failed: Intent data is null")
                return Result.failure(Exception("Google sign in failed: No data received"))
            }
            
            Log.d(TAG, "Processing Google Sign-In result")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google Sign-In successful, got account email: ${account.email}, id: ${account.id?.take(5)}...")
                Result.success(account)
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed: Error code: ${e.statusCode}", e)
                Result.failure(Exception("Google sign in failed: ${getSignInErrorMessage(e.statusCode)}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Google sign in intent error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Helper function to get meaningful error messages for Google Sign-In errors
    private fun getSignInErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            CommonStatusCodes.NETWORK_ERROR -> "Network error - check your internet connection"
            CommonStatusCodes.TIMEOUT -> "Connection timed out"
            GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign in was cancelled"
            GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign in failed"
            GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Sign in already in progress"
            else -> "Error code: $statusCode"
        }
    }
    
    // User profile management
    private suspend fun createUserProfile(
        userId: String,
        username: String,
        email: String,
        photoUrl: String?
    ) {
        try {
            val sanitizedUid = sanitizeDatabasePath(userId)
            if (sanitizedUid.isEmpty()) {
                throw IllegalArgumentException("Invalid user ID")
            }

            val userProfile = hashMapOf(
                "userId" to sanitizedUid,
                "username" to username,
                "email" to email,
                "photoUrl" to (photoUrl ?: ""),
                "totalScore" to 0,
                "quizzesTaken" to 0,
                "dateJoined" to System.currentTimeMillis()
            )
            
            usersRef.child(sanitizedUid).setValue(userProfile).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Create profile error: ${e.message}", e)
            throw e
        }
    }
    
    suspend fun updateUserScore(userId: String, score: Int) {
        try {
            // Get current user data
            val userSnapshot = usersRef.child(userId).get().await()
            
            if (userSnapshot.exists()) {
                // Update total score and increment quizzes taken
                val currentScore = userSnapshot.child("totalScore").getValue(Int::class.java) ?: 0
                val quizzesTaken = userSnapshot.child("quizzesTaken").getValue(Int::class.java) ?: 0
                
                val updates = HashMap<String, Any>()
                updates["totalScore"] = currentScore + score
                updates["quizzesTaken"] = quizzesTaken + 1
                
                usersRef.child(userId).updateChildren(updates).await()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Update score error: ${e.message}", e)
        }
    }
    
    // Sign out
    fun signOut() {
        auth.signOut()
    }
    
    // Password reset
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Password reset error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Test function to check Firebase connectivity
    suspend fun testFirebaseConnection(): Result<Boolean> {
        return try {
            Log.d(TAG, "Testing Firebase connectivity...")
            
            // Test database connection
            val testRef = database.getReference(".info/connected")
            val snapshot = testRef.get().await()
            val isConnected = snapshot.getValue(Boolean::class.java) ?: false
            Log.d(TAG, "Firebase database connection: $isConnected")

            Result.success(isConnected)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Firebase connectivity test failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Helper function to validate and sanitize database paths
    private fun sanitizeDatabasePath(path: String): String {
        return path.trim()
            .replace("[.#$\\[\\]]".toRegex(), "")  // Remove invalid characters
            .replace("//", "/")  // Remove double slashes
            .trim('/')  // Remove leading/trailing slashes
    }
}