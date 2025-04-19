package com.shk.smarty.ui.screens.quizzes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shk.smarty.di.QuizViewModelFactoryProvider
import com.shk.smarty.model.Quiz
import com.shk.smarty.ui.components.ErrorMessage
import com.shk.smarty.ui.components.LoadingIndicator
import com.shk.smarty.ui.components.SmartyScaffold
import com.shk.smarty.viewmodel.QuizViewModel
import dagger.hilt.android.EntryPointAccessors
import androidx.lifecycle.SavedStateHandle

@Composable
fun QuizListScreen(
    topicId: String,
    subjectId: String,
    moduleTitle: String,
    onQuizClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    // Get the QuizViewModel using Hilt EntryPoint
    val context = LocalContext.current
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        QuizViewModelFactoryProvider::class.java
    )
    val factory = entryPoint.quizViewModelFactory()
    
    // Create a SavedStateHandle with the required parameters
    val savedStateHandle = SavedStateHandle().apply {
        set("topicId", topicId)
        set("subjectId", subjectId)
    }
    
    val viewModel = remember(topicId, subjectId) {
        factory.create(savedStateHandle)
    }
    
    val quizzes by viewModel.quizzes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    SmartyScaffold(
        title = moduleTitle,
        showBackButton = true,
        onBackClick = onBackClick
    ) { paddingModifier ->
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(message = error ?: "Unknown error occurred")
            quizzes.isEmpty() -> EmptyQuizList()
            else -> QuizList(
                quizzes = quizzes,
                modifier = paddingModifier,
                onQuizClick = onQuizClick
            )
        }
    }
}

@Composable
fun QuizList(
    quizzes: List<Quiz>,
    modifier: Modifier = Modifier,
    onQuizClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(quizzes) { quiz ->
            QuizItem(
                quiz = quiz,
                onQuizClick = onQuizClick
            )
        }
    }
}

@Composable
fun QuizItem(
    quiz: Quiz,
    onQuizClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onQuizClick(quiz.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = quiz.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Question count
                Icon(
                    imageVector = Icons.Default.QuestionMark,
                    contentDescription = "Questions",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Use 0 as a default if there are no questions or the property doesn't exist
                val questionCount = 0
                val questionText = if (questionCount == 1) "1 Question" else "$questionCount Questions"
                
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Time
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Time",
                    tint = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Use timeLimit instead of timeInMinutes
                val timeText = "${quiz.timeLimit} min"
                
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun EmptyQuizList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "No quizzes found",
            style = MaterialTheme.typography.titleMedium
        )
        
        Text(
            text = "Quizzes will appear here once they are added",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}