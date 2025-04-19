package com.shk.smarty.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shk.smarty.model.Quiz
import com.shk.smarty.repository.NewFirebaseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel @AssistedInject constructor(
    private val repository: NewFirebaseRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val moduleId: String = savedStateHandle.get<String>("moduleId") ?: ""
    private val topicId: String = savedStateHandle.get<String>("topicId") ?: ""
    private val subjectId: String = savedStateHandle.get<String>("subjectId") ?: ""
    
    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadQuizzes()
    }
    
    private fun loadQuizzes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (topicId.isNotEmpty() && subjectId.isNotEmpty()) {
                    repository.getQuizzes(subjectId, topicId).collect { quizList ->
                        _quizzes.value = quizList
                        _isLoading.value = false
                    }
                } else {
                    _error.value = "Missing required topic or subject ID"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load quizzes: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun refreshQuizzes() {
        loadQuizzes()
    }
    
    suspend fun addQuiz(quiz: Quiz): String {
        return try {
            repository.addQuiz(quiz)
        } catch (e: Exception) {
            _error.value = "Failed to add quiz: ${e.message}"
            ""
        }
    }
}

@AssistedFactory
interface QuizViewModelAssistedFactory {
    fun create(savedStateHandle: SavedStateHandle): QuizViewModel
}