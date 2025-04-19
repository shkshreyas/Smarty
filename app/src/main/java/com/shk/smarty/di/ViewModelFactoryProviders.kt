package com.shk.smarty.di

import com.shk.smarty.viewmodel.ModuleViewModelAssistedFactory
import com.shk.smarty.viewmodel.QuestionViewModelAssistedFactory
import com.shk.smarty.viewmodel.QuizViewModelAssistedFactory
import com.shk.smarty.viewmodel.TopicViewModelAssistedFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ModuleViewModelFactoryProvider {
    fun moduleViewModelFactory(): ModuleViewModelAssistedFactory
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuizViewModelFactoryProvider {
    fun quizViewModelFactory(): QuizViewModelAssistedFactory
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuestionViewModelFactoryProvider {
    fun questionViewModelFactory(): QuestionViewModelAssistedFactory
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TopicViewModelFactoryProvider {
    fun topicViewModelFactory(): TopicViewModelAssistedFactory
} 