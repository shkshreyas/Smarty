package com.shk.smarty.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shk.smarty.model.Module
import com.shk.smarty.model.Topic
import com.shk.smarty.repository.NewFirebaseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ModuleViewModel @AssistedInject constructor(
    private val repository: NewFirebaseRepository,
    @Assisted private val subjectId: String
) : ViewModel() {
    
    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadTopics()
    }
    
    private fun loadTopics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getTopics(subjectId).collect { topicList ->
                    _topics.value = topicList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load topics: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun refreshTopics() {
        loadTopics()
    }
    
    suspend fun addTopic(topic: Topic): String {
        return try {
            repository.addTopic(topic)
        } catch (e: Exception) {
            _error.value = "Failed to add topic: ${e.message}"
            ""
        }
    }
    
    suspend fun updateSubject(topic: Topic) {
        try {
            // Not implemented in repository yet
            _error.value = "Update functionality not yet implemented"
        } catch (e: Exception) {
            _error.value = "Failed to update topic: ${e.message}"
        }
    }
    
    suspend fun deleteTopic(topicId: String) {
        try {
            // Not implemented in repository yet
            _error.value = "Delete functionality not yet implemented"
        } catch (e: Exception) {
            _error.value = "Failed to delete topic: ${e.message}"
        }
    }
}

@AssistedFactory
interface ModuleViewModelAssistedFactory {
    fun create(subjectId: String): ModuleViewModel
}