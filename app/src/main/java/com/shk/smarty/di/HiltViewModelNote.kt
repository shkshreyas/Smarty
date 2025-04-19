package com.shk.smarty.di

/**
 * HILT VIEWMODEL MIGRATION
 * =========================
 * 
 * The app previously used AssistedInject to create ViewModels with parameters.
 * To simplify the architecture and fix crashes, the implementation has been migrated
 * to use Hilt's @HiltViewModel with SavedStateHandle instead.
 * 
 * Changes made:
 * 
 * 1. ViewModels (ModuleViewModel, QuizViewModel, QuestionViewModel) now:
 *    - Use @HiltViewModel annotation
 *    - Inject SavedStateHandle
 *    - Extract parameters from SavedStateHandle instead of constructor parameters
 * 
 * 2. Removed:
 *    - AssistedFactory interfaces
 *    - Assisted injection bindings
 *    - ViewModelFactoryProvider (now deprecated)
 * 
 * 3. All screens continue to use:
 *    - hiltViewModel() to obtain viewmodels
 *    - Navigation parameters to pass data between screens
 * 
 * The navigation component automatically puts route parameters into SavedStateHandle,
 * so we simply read them from there in the ViewModel.
 * 
 * Example:
 * 
 * ```
 * @HiltViewModel
 * class ModuleViewModel @Inject constructor(
 *     repository: FirebaseRepository,
 *     savedStateHandle: SavedStateHandle
 * ) : ViewModel() {
 *     private val subjectId: String = savedStateHandle.get<String>("subjectId") ?: ""
 *     // Rest of the ViewModel implementation...
 * }
 * ```
 * 
 * Navigation Component already places the "subjectId" route parameter into SavedStateHandle
 * so the ViewModel can read it directly.
 */ 