package com.shk.smarty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shk.smarty.model.Subject
import com.shk.smarty.repository.NewFirebaseRepository
import com.shk.smarty.util.DataImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: NewFirebaseRepository
) : ViewModel() {
    
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadSubjects()
    }
    
    private fun loadSubjects() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Test database connection first - run on IO dispatcher to avoid blocking main thread
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val connectionResult = repository.testDatabaseConnection()
                    connectionResult.onFailure { exception ->
                        _error.value = "Connection error: ${exception.message}"
                        _isLoading.value = false
                        return@withContext
                    }
                }
                
                // Only proceed if we didn't encounter a connection error
                if (_error.value == null) {
                    repository.getSubjects().collect { subjectList -> 
                        _subjects.value = subjectList
                        
                        // Check if we have data
                        if (subjectList.isEmpty()) {
                            _error.value = "No subjects found. Would you like to import sample data?"
                        } else {
                            _error.value = null
                        }
                        
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load subjects: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun refreshSubjects() {
        loadSubjects()
    }
    
    fun importSampleData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = DataImporter.importHistoryQuizData()
                result.onSuccess { 
                    loadSubjects() 
                }.onFailure { exception ->
                    _error.value = "Failed to import sample data: ${exception.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to import sample data: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    suspend fun addSubject(subject: Subject): String {
        return try {
            repository.addSubject(subject)
        } catch (e: Exception) {
            _error.value = "Failed to add subject: ${e.message}"
            ""
        }
    }
    
    suspend fun updateSubject(subject: Subject) {
        try {
            repository.updateSubject(subject)
        } catch (e: Exception) {
            _error.value = "Failed to update subject: ${e.message}"
        }
    }
    
    suspend fun deleteSubject(subjectId: String) {
        try {
            repository.deleteSubject(subjectId)
        } catch (e: Exception) {
            _error.value = "Failed to delete subject: ${e.message}"
        }
    }
}