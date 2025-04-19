package com.shk.smarty.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.shk.smarty.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    
    // Authentication state
    val currentUser = repository.currentUser
    
    // UI state for login/signup
    data class AuthState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false
    )
    
    private val _loginState = MutableStateFlow(AuthState())
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()
    
    private val _signupState = MutableStateFlow(AuthState())
    val signupState: StateFlow<AuthState> = _signupState.asStateFlow()
    
    private val _resetPasswordState = MutableStateFlow(AuthState())
    val resetPasswordState: StateFlow<AuthState> = _resetPasswordState.asStateFlow()
    
    // Form validation state
    data class ValidationState(
        val isEmailValid: Boolean = true,
        val isPasswordValid: Boolean = true,
        val isUsernameValid: Boolean = true,
        val emailError: String? = null,
        val passwordError: String? = null,
        val usernameError: String? = null
    )
    
    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()
    
    // Email & Password Authentication
    fun signInWithEmail(email: String, password: String) {
        if (!validateLoginForm(email, password)) return
        
        _loginState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            repository.signInWithEmail(email, password)
                .onSuccess {
                    _loginState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { exception ->
                    _loginState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = exception.message ?: "Failed to sign in" 
                        )
                    }
                }
        }
    }
    
    fun signUpWithEmail(email: String, password: String, username: String) {
        if (!validateSignupForm(email, password, username)) return
        
        _signupState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            repository.signUpWithEmail(email, password, username)
                .onSuccess {
                    _signupState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { exception ->
                    _signupState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = exception.message ?: "Failed to sign up" 
                        )
                    }
                }
        }
    }
    
    // Google Sign In Handling
    fun handleGoogleSignInResult(data: Intent?) {
        _loginState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            repository.handleSignInResult(data)
                .onSuccess { account ->
                    firebaseAuthWithGoogle(account)
                }
                .onFailure { exception ->
                    _loginState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = exception.message ?: "Google sign-in failed" 
                        )
                    }
                }
        }
    }
    
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        repository.firebaseAuthWithGoogle(account)
            .onSuccess {
                _loginState.update { it.copy(isLoading = false, isSuccess = true) }
            }
            .onFailure { exception ->
                _loginState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = exception.message ?: "Failed to authenticate with Firebase" 
                    )
                }
            }
    }
    
    // Password Reset
    fun sendPasswordResetEmail(email: String) {
        if (!validateEmail(email)) return
        
        _resetPasswordState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            repository.sendPasswordResetEmail(email)
                .onSuccess {
                    _resetPasswordState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { exception ->
                    _resetPasswordState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = exception.message ?: "Failed to send reset email" 
                        )
                    }
                }
        }
    }
    
    // Sign Out
    fun signOut() {
        repository.signOut()
    }
    
    // Form Validation
    private fun validateLoginForm(email: String, password: String): Boolean {
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)
        
        return isEmailValid && isPasswordValid
    }
    
    private fun validateSignupForm(email: String, password: String, username: String): Boolean {
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)
        val isUsernameValid = validateUsername(username)
        
        return isEmailValid && isPasswordValid && isUsernameValid
    }
    
    private fun validateEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val isValid = email.matches(emailPattern.toRegex())
        
        _validationState.update { 
            it.copy(
                isEmailValid = isValid,
                emailError = if (isValid) null else "Please enter a valid email address"
            ) 
        }
        
        return isValid
    }
    
    private fun validatePassword(password: String): Boolean {
        val isValid = password.length >= 6
        
        _validationState.update { 
            it.copy(
                isPasswordValid = isValid,
                passwordError = if (isValid) null else "Password must be at least 6 characters"
            ) 
        }
        
        return isValid
    }
    
    private fun validateUsername(username: String): Boolean {
        val isValid = username.length >= 3
        
        _validationState.update { 
            it.copy(
                isUsernameValid = isValid,
                usernameError = if (isValid) null else "Username must be at least 3 characters"
            ) 
        }
        
        return isValid
    }
    
    // Reset UI states
    fun resetLoginState() {
        _loginState.value = AuthState()
    }
    
    fun resetSignupState() {
        _signupState.value = AuthState()
    }
    
    fun resetPasswordResetState() {
        _resetPasswordState.value = AuthState()
    }
    
    fun resetValidationState() {
        _validationState.value = ValidationState()
    }
    
    // Test Firebase connectivity
    suspend fun testFirebaseConnection(): String {
        return try {
            val result = repository.testFirebaseConnection()
            result.fold(
                onSuccess = { isConnected -> 
                    if (isConnected) {
                        "Connection successful: Firebase is reachable"
                    } else {
                        "Connection error: Firebase is not reachable"
                    }
                },
                onFailure = { exception ->
                    "Connection error: ${exception.message}"
                }
            )
        } catch (e: Exception) {
            "Connection error: ${e.message}"
        }
    }
}