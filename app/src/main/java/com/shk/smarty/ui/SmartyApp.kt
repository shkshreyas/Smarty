package com.shk.smarty.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shk.smarty.ui.screens.QuestionScreen
import com.shk.smarty.ui.screens.auth.ForgotPasswordScreen
import com.shk.smarty.ui.screens.auth.LoginScreen
import com.shk.smarty.ui.screens.auth.SignupScreen
import com.shk.smarty.ui.screens.auth.WelcomeScreen
import com.shk.smarty.ui.screens.modules.ModuleScreen
import com.shk.smarty.ui.screens.quizzes.QuizListScreen
import com.shk.smarty.ui.screens.subjects.SubjectScreen
import com.shk.smarty.viewmodel.AuthViewModel

@Composable
fun SmartyApp(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // Get current auth state
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Determine start destination based on auth state
    val startDestination = if (currentUser == null) {
        Screen.Welcome.route
    } else {
        Screen.Subjects.route
    }
    
    // Animation durations
    val animationDuration = 500
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(
            route = Screen.Welcome.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(animationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(animationDuration)
                )
            }
        ) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onSignupClick = { navController.navigate(Screen.Signup.route) }
            )
        }
        
        composable(
            route = Screen.Login.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                )
            }
        ) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = { 
                    navController.navigate(Screen.Subjects.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Signup.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                )
            }
        ) {
            SignupScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onSignupSuccess = {
                    navController.navigate(Screen.Subjects.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.ForgotPassword.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                )
            }
        ) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onResetSent = { navController.popBackStack() }
            )
        }
        
        // Main App Screens
        composable(
            route = Screen.Subjects.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                )
            }
        ) {
            SubjectScreen(
                onSubjectClick = { subjectId, subjectTitle ->
                    navController.navigate(Screen.Modules.createRoute(subjectId, subjectTitle))
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Subjects.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Module screen
        composable(
            route = Screen.Modules.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("subjectTitle") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                )
            }
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val subjectTitle = backStackEntry.arguments?.getString("subjectTitle") ?: ""
            
            ModuleScreen(
                subjectId = subjectId,
                subjectTitle = subjectTitle,
                onModuleClick = { topicId, topicTitle ->
                    navController.navigate(Screen.Quizzes.createRoute(topicId, topicTitle, subjectId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Quiz list screen
        composable(
            route = Screen.Quizzes.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType },
                navArgument("topicTitle") { type = NavType.StringType },
                navArgument("subjectId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                )
            }
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            val topicTitle = backStackEntry.arguments?.getString("topicTitle") ?: ""
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            
            QuizListScreen(
                topicId = topicId,
                subjectId = subjectId,
                moduleTitle = topicTitle,
                onQuizClick = { quizId ->
                    navController.navigate(Screen.Questions.createRoute(quizId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Quiz questions screen
        composable(
            route = Screen.Questions.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                )
            }
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            
            QuestionScreen(
                quizId = quizId,
                onBackClick = {
                    navController.popBackStack()
                },
                onFinish = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object Subjects : Screen("subjects")
    
    object Modules : Screen("modules/{subjectId}/{subjectTitle}") {
        fun createRoute(subjectId: String, subjectTitle: String): String {
            return "modules/$subjectId/$subjectTitle"
        }
    }
    
    object Quizzes : Screen("quizzes/{topicId}/{topicTitle}/{subjectId}") {
        fun createRoute(topicId: String, topicTitle: String, subjectId: String): String {
            return "quizzes/$topicId/$topicTitle/$subjectId"
        }
    }
    
    object Questions : Screen("questions/{quizId}") {
        fun createRoute(quizId: String): String {
            return "questions/$quizId"
        }
    }
}