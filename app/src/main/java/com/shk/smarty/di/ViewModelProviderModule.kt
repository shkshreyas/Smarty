package com.shk.smarty.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ViewModelProviderModule {
    
    @Provides
    @Singleton
    fun provideViewModelFactoryProvider(
        moduleViewModelFactory: ModuleViewModelAssistedFactory,
        quizViewModelFactory: QuizViewModelAssistedFactory,
        questionViewModelFactory: QuestionViewModelAssistedFactory,
        topicViewModelFactory: TopicViewModelAssistedFactory
    ): ViewModelFactoryProvider {
        return ViewModelFactoryProvider(
            moduleViewModelFactory,
            quizViewModelFactory,
            questionViewModelFactory,
            topicViewModelFactory
        )
    }
} 