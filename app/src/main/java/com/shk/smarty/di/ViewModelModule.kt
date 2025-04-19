package com.shk.smarty.di

/**
 * This module is no longer needed since we're now using @HiltViewModel annotation.
 * 
 * Previously, this module manually created ViewModels with dependencies,
 * but now Hilt automatically handles ViewModel creation with @HiltViewModel.
 * 
 * The ViewModels now obtain the SavedStateHandle directly through constructor injection
 * and fetch parameters from it.
 * 
 * See:
 * - com.shk.smarty.viewmodel.ModuleViewModel
 * - com.shk.smarty.viewmodel.QuizViewModel
 * - com.shk.smarty.viewmodel.QuestionViewModel
 */ 