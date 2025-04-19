package com.shk.smarty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shk.smarty.repository.FirebaseRepository
import javax.inject.Inject

class ViewModelFactory {
    
    class ModuleViewModelFactory(
        private val repository: FirebaseRepository,
        private val subjectId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ModuleViewModel::class.java)) {
                // This will likely never be used since we're now using HiltViewModel
                // but keeping for compatibility with existing code
                throw IllegalArgumentException("Use Hilt to obtain ModuleViewModel")
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    class QuizViewModelFactory(
        private val repository: FirebaseRepository,
        private val moduleId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
                // This will likely never be used since we're now using HiltViewModel
                // but keeping for compatibility with existing code
                throw IllegalArgumentException("Use Hilt to obtain QuizViewModel")
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    class QuestionViewModelFactory(
        private val repository: FirebaseRepository,
        private val quizId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuestionViewModel::class.java)) {
                // This will likely never be used since we're now using HiltViewModel
                // but keeping for compatibility with existing code
                throw IllegalArgumentException("Use Hilt to obtain QuestionViewModel")
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    class ModuleFactoryCreator @Inject constructor(
        private val repository: FirebaseRepository
    ) {
        fun create(subjectId: String): ModuleViewModelFactory {
            return ModuleViewModelFactory(repository, subjectId)
        }
    }
    
    class QuizFactoryCreator @Inject constructor(
        private val repository: FirebaseRepository
    ) {
        fun create(moduleId: String): QuizViewModelFactory {
            return QuizViewModelFactory(repository, moduleId)
        }
    }
    
    class QuestionFactoryCreator @Inject constructor(
        private val repository: FirebaseRepository
    ) {
        fun create(quizId: String): QuestionViewModelFactory {
            return QuestionViewModelFactory(repository, quizId)
        }
    }
}