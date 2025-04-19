package com.shk.smarty.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shk.smarty.model.Topic
import com.shk.smarty.repository.NewFirebaseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TopicViewModel @AssistedInject constructor(
    private val repository: NewFirebaseRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _topicsState = MutableStateFlow<List<Topic>>(emptyList())
    val topicsState: StateFlow<List<Topic>> = _topicsState

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private var _currentSubjectId: String? = null

    init {
        // You can use savedStateHandle to restore state
        _currentSubjectId = savedStateHandle.get<String>("subjectId")
        _currentSubjectId?.let { loadTopics(it) }
    }

    fun loadTopics(subjectId: String) {
        _currentSubjectId = subjectId
        _loadingState.value = true
        _errorState.value = null
        
        // Save to state handle for restoration
        savedStateHandle["subjectId"] = subjectId

        viewModelScope.launch {
            try {
                repository.getTopics(subjectId)
                    .catch { exception ->
                        _loadingState.value = false
                        _errorState.value = "Error loading topics: ${exception.message}"
                    }
                    .collectLatest { topics ->
                        _topicsState.value = topics
                        _loadingState.value = false
                    }
            } catch (e: Exception) {
                _loadingState.value = false
                _errorState.value = "Error loading topics: ${e.message}"
            }
        }
    }

    fun refreshTopics() {
        _currentSubjectId?.let { loadTopics(it) }
    }

    fun addTopic(topic: Topic) {
        viewModelScope.launch {
            try {
                repository.addTopic(topic)
                refreshTopics()
            } catch (e: Exception) {
                _errorState.value = "Error adding topic: ${e.message}"
            }
        }
    }

    // Methods commented out as they reference methods not available in the repository
    /*
    fun updateTopic(topic: Topic) {
        viewModelScope.launch {
            try {
                repository.updateTopic(topic)
                refreshTopics()
            } catch (e: Exception) {
                _errorState.value = "Error updating topic: ${e.message}"
            }
        }
    }

    fun deleteTopic(topicId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTopic(topicId)
                refreshTopics()
            } catch (e: Exception) {
                _errorState.value = "Error deleting topic: ${e.message}"
            }
        }
    }
    */
}

@AssistedFactory
interface TopicViewModelAssistedFactory {
    fun create(savedStateHandle: SavedStateHandle): TopicViewModel
} 