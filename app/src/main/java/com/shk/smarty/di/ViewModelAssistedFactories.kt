package com.shk.smarty.di

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.shk.smarty.viewmodel.ModuleViewModel
import com.shk.smarty.viewmodel.QuestionViewModel
import com.shk.smarty.viewmodel.QuizViewModel
import com.shk.smarty.viewmodel.TopicViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
interface ModuleViewModelAssistedFactory {
    fun create(subjectId: String): ModuleViewModel
}

@AssistedFactory
interface QuizViewModelAssistedFactory {
    fun create(savedStateHandle: SavedStateHandle): QuizViewModel
}

@AssistedFactory
interface QuestionViewModelAssistedFactory {
    fun create(savedStateHandle: SavedStateHandle): QuestionViewModel
}

@AssistedFactory
interface TopicViewModelAssistedFactory {
    fun create(savedStateHandle: SavedStateHandle): TopicViewModel
}

// Create view model factory that uses the assisted factory to create the view model
inline fun <VM : ViewModel> assistedViewModelFactory(
    crossinline create: (SavedStateHandle) -> VM
): AbstractSavedStateViewModelFactory =
    object : AbstractSavedStateViewModelFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T = create(handle) as T
    }

// extension function to create view model factory from assisted factory
inline fun <VM : ViewModel> assistedFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null,
    crossinline create: (SavedStateHandle) -> VM
): AbstractSavedStateViewModelFactory =
    object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T = create(handle) as T
    } 