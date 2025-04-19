package com.shk.smarty.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Module for Assisted Factories that create ViewModels with runtime parameters.
 *
 * This module doesn't need to provide explicit bindings for @AssistedFactory interfaces
 * as Dagger/Hilt automatically handles their registration.
 *
 * The following AssistedFactory interfaces are used:
 * - ModuleViewModelAssistedFactory: Creates ModuleViewModel with SavedStateHandle
 * - QuizViewModelAssistedFactory: Creates QuizViewModel with SavedStateHandle
 * - QuestionViewModelAssistedFactory: Creates QuestionViewModel with SavedStateHandle
 * - TopicViewModelAssistedFactory: Creates TopicViewModel with SavedStateHandle
 *
 * Each ViewModel retrieves its parameters (subjectId, moduleId, quizId) from the
 * SavedStateHandle, which is populated by the navigation component.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ViewModelAssistedFactoriesModule {
    // No explicit bindings needed for @AssistedFactory interfaces
    // Dagger/Hilt automatically handles registration of these interfaces
} 