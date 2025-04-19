package com.shk.smarty.di

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject

/**
 * Factory provider that creates view model factories which use assisted injection
 * to create ViewModels with SavedStateHandle parameter.
 * 
 * This provider works with the AssistedFactory interfaces to facilitate
 * the creation of ViewModels that require runtime parameters through SavedStateHandle.
 */
class ViewModelFactoryProvider @Inject constructor(
    private val moduleViewModelFactory: ModuleViewModelAssistedFactory,
    private val quizViewModelFactory: QuizViewModelAssistedFactory,
    private val questionViewModelFactory: QuestionViewModelAssistedFactory,
    private val topicViewModelFactory: TopicViewModelAssistedFactory
) {
    /**
     * Creates a SavedStateViewModelFactory for a specific ViewModel type
     */
    fun create(owner: SavedStateRegistryOwner): ViewModelProvider.Factory {
        return object : AbstractSavedStateViewModelFactory(owner, null) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return when {
                    modelClass.isAssignableFrom(com.shk.smarty.viewmodel.ModuleViewModel::class.java) ->
                        moduleViewModelFactory.create(handle.get<String>("subjectId") ?: "") as T
                    modelClass.isAssignableFrom(com.shk.smarty.viewmodel.QuizViewModel::class.java) ->
                        quizViewModelFactory.create(handle) as T
                    modelClass.isAssignableFrom(com.shk.smarty.viewmodel.QuestionViewModel::class.java) ->
                        questionViewModelFactory.create(handle) as T
                    modelClass.isAssignableFrom(com.shk.smarty.viewmodel.TopicViewModel::class.java) ->
                        topicViewModelFactory.create(handle) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}