package com.shk.smarty.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shk.smarty.model.Question
import com.shk.smarty.repository.FirebaseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionViewModel @AssistedInject constructor(
    private val repository: FirebaseRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val quizId: String = savedStateHandle.get<String>("quizId") ?: ""
    
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // For tracking quiz progress
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()
    
    // For tracking user answers and score
    private val userAnswers = mutableMapOf<Int, Int>() // Question index to selected option index
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()
    
    init {
        loadQuestions()
    }
    
    private fun loadQuestions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getQuestionsByQuiz(quizId).collect { questionList ->
                    _questions.value = questionList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load questions: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun refreshQuestions() {
        loadQuestions()
    }
    
    suspend fun addQuestion(question: Question): String {
        return try {
            repository.addQuestion(question)
        } catch (e: Exception) {
            _error.value = "Failed to add question: ${e.message}"
            ""
        }
    }
    
    suspend fun updateQuestion(question: Question) {
        try {
            repository.updateQuestion(question)
        } catch (e: Exception) {
            _error.value = "Failed to update question: ${e.message}"
        }
    }
    
    // Quiz navigation and scoring
    fun moveToNextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value = _currentQuestionIndex.value + 1
        }
    }
    
    fun moveToPreviousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value = _currentQuestionIndex.value - 1
        }
    }
    
    fun resetQuiz() {
        _currentQuestionIndex.value = 0
        userAnswers.clear()
        _score.value = 0
    }
    
    fun submitAnswer(selectedOptionIndex: Int) {
        // Store the user's answer for current question
        userAnswers[_currentQuestionIndex.value] = selectedOptionIndex
        
        // Calculate the current score based on all answered questions
        calculateScore()
    }
    
    // Calculate score based on all submitted answers
    private fun calculateScore() {
        val questions = _questions.value
        var correctAnswers = 0
        
        for ((questionIndex, selectedOptionIndex) in userAnswers) {
            if (questionIndex < questions.size) {
                val question = questions[questionIndex]
                if (selectedOptionIndex == question.correctOptionIndex) {
                    correctAnswers++
                }
            }
        }
        
        _score.value = correctAnswers
    }
    
    // Get the final score when quiz is finished
    fun getFinalScore(): Int {
        calculateScore() // Ensure score is up to date
        
        // Double-check by manually counting correct answers to verify
        var finalScore = 0
        val questions = _questions.value
        
        for ((questionIndex, selectedOptionIndex) in userAnswers) {
            if (questionIndex < questions.size) {
                val question = questions[questionIndex]
                if (selectedOptionIndex == question.correctOptionIndex) {
                    finalScore++
                }
            }
        }
        
        // Log or handle discrepancies if any
        if (finalScore != _score.value) {
            _score.value = finalScore // Correct any inconsistency
        }
        
        return _score.value
    }
    
    fun getCurrentQuestion(): Question? {
        return if (_questions.value.isNotEmpty() && _currentQuestionIndex.value < _questions.value.size) {
            _questions.value[_currentQuestionIndex.value]
        } else {
            null
        }
    }
    
    fun isLastQuestion(): Boolean {
        return _currentQuestionIndex.value == _questions.value.size - 1
    }
    
    fun isFirstQuestion(): Boolean {
        return _currentQuestionIndex.value == 0
    }
    
    fun getQuizProgress(): Float {
        return if (_questions.value.isEmpty()) {
            0f
        } else {
            _currentQuestionIndex.value.toFloat() / _questions.value.size.toFloat()
        }
    }
}

@AssistedFactory
interface QuestionViewModelAssistedFactory {
    fun create(savedStateHandle: SavedStateHandle): QuestionViewModel
}