# Smarty App Migration to Jetpack Compose

This guide outlines the steps to migrate the existing SmartyParty quiz application from the traditional View-based UI to Jetpack Compose. The migration will maintain all existing functionality while modernizing the codebase.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Setup and Dependencies](#setup-and-dependencies)
3. [Migration Strategy](#migration-strategy)
4. [UI Components Migration](#ui-components-migration)
5. [Navigation Implementation](#navigation-implementation)
6. [Firebase Integration](#firebase-integration)
7. [Testing and Validation](#testing-and-validation)

## Project Overview

The SmartyParty app is a quiz application with the following structure:

- **Subjects**: Top-level categories containing modules
- **Modules**: Collections of quizzes within a subject
- **Quizzes**: Sets of questions with multiple-choice answers
- **Questions**: Individual quiz items with options and correct answers

The app uses Firebase Realtime Database for data storage and retrieval.

## Setup and Dependencies

### 1. Update Gradle Dependencies

Add the following dependencies to your `app/build.gradle.kts` file:

```kotlin
dependencies {
    // Existing dependencies...
    
    // Compose dependencies
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("io.coil-kt:coil-compose:2.5.0") // For image loading
    
    // For testing Compose
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### 2. Enable Compose in your project

Update the `android` block in your `app/build.gradle.kts`:

```kotlin
android {
    // Existing configuration...
    
    buildFeatures {
        viewBinding = true
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}
```

### 3. Update themes.xml

Create a new theme for Compose in your `themes.xml` file:

```xml
<style name="Theme.SmartyParty.Compose" parent="android:Theme.Material.Light.NoActionBar">
    <item name="android:statusBarColor">@color/purple_700</item>
</style>
```

## Migration Strategy

The migration will follow these steps:

1. Create Compose versions of all screens while keeping the existing View-based implementation
2. Implement navigation using the Navigation Compose library
3. Create Compose UI components to replace XML layouts
4. Migrate data fetching logic to ViewModel classes
5. Test each screen and ensure feature parity
6. Remove legacy code once migration is complete

## UI Components Migration

### 1. Define Theme and Colors

Create a theme file (`ui/theme/Theme.kt`):

```kotlin
package com.shk.smartyparty.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC5),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun SmartyPartyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
```

Create a typography file (`ui/theme/Type.kt`):

```kotlin
package com.shk.smartyparty.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
)
```

### 2. Create Model Classes

Keep the existing model classes as they are data classes and work well with Compose:

- `SubjectModel`
- `ModuleModel`
- `QuizModel`
- `QuestionModel`

### 3. Create Composable UI Components

#### Subject List Screen

```kotlin
package com.shk.smartyparty.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shk.smartyparty.SubjectModel

@Composable
fun SubjectListScreen(
    subjects: List<SubjectModel>,
    isLoading: Boolean,
    onSubjectClick: (SubjectModel) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(subjects) { subject ->
                    SubjectItem(subject = subject, onClick = { onSubjectClick(subject) })
                }
            }
        }
    }
}

@Composable
fun SubjectItem(subject: SubjectModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subject.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = subject.description)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${subject.modules.size} Modules",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

#### Module List Screen

```kotlin
package com.shk.smartyparty.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shk.smartyparty.ModuleModel

@Composable
fun ModuleListScreen(
    subjectTitle: String,
    modules: List<ModuleModel>,
    isLoading: Boolean,
    onModuleClick: (ModuleModel) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = subjectTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(modules) { module ->
                        ModuleItem(module = module, onClick = { onModuleClick(module) })
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleItem(module: ModuleModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = module.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = module.description)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${module.quizzes.size} Quizzes",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

#### Quiz List Screen

```kotlin
package com.shk.smartyparty.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shk.smartyparty.QuizModel

@Composable
fun QuizListScreen(
    moduleTitle: String,
    quizzes: List<QuizModel>,
    isLoading: Boolean,
    onQuizClick: (QuizModel) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = moduleTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(quizzes) { quiz ->
                        QuizItem(quiz = quiz, onClick = { onQuizClick(quiz) })
                    }
                }
            }
        }
    }
}

@Composable
fun QuizItem(quiz: QuizModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = quiz.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = quiz.subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${quiz.questionList.size} Questions",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Time: ${quiz.time} min",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
```

#### Quiz Screen

```kotlin
package com.shk.smartyparty.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shk.smartyparty.QuestionModel
import com.shk.smartyparty.QuizModel
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    quiz: QuizModel,
    onBackClick: () -> Unit,
    onFinish: (Int, Int) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf("") }
    var score by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(quiz.time.toInt() * 60) }
    var isFinished by remember { mutableStateOf(false) }
    
    // Timer effect
    LaunchedEffect(key1 = Unit) {
        while (timeRemaining > 0 && !isFinished) {
            delay(1000)
            timeRemaining--
        }
        if (timeRemaining <= 0 && !isFinished) {
            isFinished = true
            onFinish(score, quiz.questionList.size)
        }
    }
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = quiz.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = String.format("%02d:%02d", timeRemaining / 60, timeRemaining % 60),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        if (currentQuestionIndex < quiz.questionList.size) {
            QuestionContent(
                modifier = Modifier.padding(paddingValues),
                questionModel = quiz.questionList[currentQuestionIndex],
                questionNumber = currentQuestionIndex + 1,
                totalQuestions = quiz.questionList.size,
                selectedAnswer = selectedAnswer,
                onAnswerSelected = { selectedAnswer = it },
                onNextQuestion = {
                    if (selectedAnswer == quiz.questionList[currentQuestionIndex].correct) {
                        score++
                    }
                    selectedAnswer = ""
                    currentQuestionIndex++
                    
                    if (currentQuestionIndex >= quiz.questionList.size) {
                        isFinished = true
                        onFinish(score, quiz.questionList.size)
                    }
                }
            )
        }
    }
}

@Composable
fun QuestionContent(
    modifier: Modifier = Modifier,
    questionModel: QuestionModel,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: String,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Progress indicator
        LinearProgressIndicator(
            progress = questionNumber.toFloat() / totalQuestions.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Question $questionNumber / $totalQuestions",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Question text
        Text(
            text = questionModel.question,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Options
        questionModel.options.forEachIndexed { index, option ->
            val isSelected = option == selectedAnswer
            val buttonColors = if (isSelected) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            }
            
            Button(
                onClick = { onAnswerSelected(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = buttonColors,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Next button
        Button(
            onClick = onNextQuestion,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = selectedAnswer.isNotEmpty()
        ) {
            Text(text = "Next")
        }
    }
}

@Composable
fun ScoreDialog(
    score: Int,
    totalQuestions: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quiz Completed!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Your Score",
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$score / $totalQuestions",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Done")
                }
            }
        }
    }
}
```

## Navigation Implementation

### Create a Navigation Graph

```kotlin
package com.shk.smartyparty.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shk.smartyparty.ui.screens.*
import com.shk.smartyparty.viewmodels.*

@Composable
fun SmartyPartyNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "subjects"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        // Subjects Screen
        composable("subjects") {
            val viewModel = remember { SubjectViewModel() }
            SubjectListScreen(
                subjects = viewModel.subjects,
                isLoading = viewModel.isLoading,
                onSubjectClick = { subject ->
                    navController.navigate("modules/${subject.id}/${subject.title}")
                }
            )
        }
        
        // Modules Screen
        composable(
            route = "modules/{subjectId}/{subjectTitle}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("subjectTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val subjectTitle = backStackEntry.arguments?.getString("subjectTitle") ?: ""
            val viewModel = remember { ModuleViewModel(subjectId) }
            
            ModuleListScreen(
                subjectTitle = subjectTitle,
                modules = viewModel.modules,
                isLoading = viewModel.isLoading,
                onModuleClick = { module ->
                    navController.navigate("quizzes/${module.id}/${module.title}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Quizzes Screen
        composable(
            route = "quizzes/{moduleId}/{moduleTitle}",
            arguments = listOf(
                navArgument("moduleId") { type = NavType.StringType },
                navArgument("moduleTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            val moduleTitle = backStackEntry.arguments?.getString("moduleTitle") ?: ""
            val viewModel = remember { QuizListViewModel(moduleId) }
            
            QuizListScreen(
                moduleTitle = moduleTitle,
                quizzes = viewModel.quizzes,
                isLoading = viewModel.isLoading,
                onQuizClick = { quiz ->
                    navController.navigate("quiz/${quiz.id}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Quiz Screen
        composable(
            route = "quiz/{quizId}",
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            val viewModel = remember { QuizViewModel(quizId) }
            
            if (viewModel.quiz != null) {
                QuizScreen(
                    quiz = viewModel.quiz!!,
                    onBackClick = { navController.popBackStack() },
                    onFinish = { score, total ->
                        // Handle quiz completion
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
```

## Firebase Integration

### Create ViewModels for Data Fetching

```kotlin
package com.shk.smartyparty.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.shk.smartyparty.SubjectModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SubjectViewModel : ViewModel() {
    var subjects by mutableStateOf<List<SubjectModel>>(emptyList())
    var isLoading by mutableStateOf(true)
    
    init {
        loadSubjects()
    }
    
    private fun loadSubjects() {
        viewModelScope.launch {
            try {
                val databaseReference = FirebaseDatabase.getInstance().reference.child("subjects")
                val dataSnapshot = databaseReference.get().await()
                
                val subjectList = mutableListOf<SubjectModel>()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val subjectModel = snapshot.getValue(SubjectModel::class.java)
                        if (subjectModel != null) {
                            subjectList.add(subjectModel)
                        }
                    }
                }
                
                subjects = subjectList
                isLoading = false
            } catch (e: Exception) {
                // Handle error
                isLoading = false
            }
        }
    }
}
```

Similar ViewModels should be created for `ModuleViewModel`, `QuizListViewModel`, and `QuizViewModel`.

## Testing and Validation

### 1. Create a Compose MainActivity

```kotlin
package com.shk.smartyparty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shk.smartyparty.navigation.SmartyPartyNavGraph
import com.shk.smartyparty.ui.theme.SmartyPartyTheme

class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartyPartyTheme {
                SmartyPartyNavGraph()
            }
        }
    }
}
```

### 2. Update AndroidManifest.xml

Update your AndroidManifest.xml to use the new Compose activity:

```xml
<activity
    android:name=".MainActivityCompose"
    android:exported="true"
    android:theme="@style/Theme.SmartyParty.Compose">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### 3. Test Each Screen

Test each screen to ensure it functions correctly:

1. Subject list loads and displays correctly
2. Navigation to modules works
3. Module list displays correctly
4. Navigation to quizzes works
5. Quiz list displays correctly
6. Quiz functionality works (timer, questions, scoring)

### 4. Gradual Migration

Consider a gradual migration approach: