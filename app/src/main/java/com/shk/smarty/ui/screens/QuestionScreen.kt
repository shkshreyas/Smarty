package com.shk.smarty.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shk.smarty.ui.components.ErrorMessage
import com.shk.smarty.ui.components.LoadingIndicator
import com.shk.smarty.ui.components.SmartyScaffold
import com.shk.smarty.ui.theme.CorrectAnswer
import com.shk.smarty.ui.theme.OnPrimary
import com.shk.smarty.ui.theme.Primary
import com.shk.smarty.ui.theme.QuizOptionSelected
import com.shk.smarty.ui.theme.QuizOptionUnselected
import com.shk.smarty.ui.theme.WrongAnswer
import com.shk.smarty.viewmodel.QuestionViewModel
import kotlinx.coroutines.delay

@Composable
fun QuestionScreen(
    quizId: String,
    onFinish: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: QuestionViewModel = hiltViewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var showAnswer by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableIntStateOf(600) } // 10 minutes default
    var finalScore by remember { mutableIntStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    // Calculate final score when showing results
    LaunchedEffect(showResults) {
        if (showResults) {
            finalScore = viewModel.getFinalScore()
        }
    }
    
    // Timer effect
    LaunchedEffect(key1 = Unit) {
        while (timeRemaining > 0 && !showResults) {
            delay(1000)
            timeRemaining--
        }
        
        if (timeRemaining <= 0 && !showResults) {
            finalScore = viewModel.getFinalScore()
            showResults = true
        }
    }
    
    val formatTime = { time: Int ->
        val minutes = time / 60
        val seconds = time % 60
        String.format("%02d:%02d", minutes, seconds)
    }
    
    val currentQuestion = viewModel.getCurrentQuestion()
    
    // Handle back button
    val handleBackPress = {
        if (!showResults) {
            showExitDialog = true
        } else {
            onBackClick()
        }
    }
    
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = onBackClick,
            onDismiss = { showExitDialog = false }
        )
    }
    
    SmartyScaffold(
        title = "Quiz",
        showBackButton = true,
        onBackClick = handleBackPress
    ) { paddingModifier ->
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(message = error ?: "Unknown error occurred")
            questions.isEmpty() -> EmptyQuestionList()
            showResults -> ResultScreen(
                score = finalScore,
                totalQuestions = questions.size,
                onFinish = onFinish
            )
            else -> {
                Column(
                    modifier = paddingModifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Timer and progress
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${currentQuestionIndex + 1}/${questions.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Time Remaining",
                                        tint = if (timeRemaining < 60) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = formatTime(timeRemaining),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (timeRemaining < 60) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Progress indicator with animation
                            val progress = (currentQuestionIndex + 1).toFloat() / questions.size.toFloat()
                            val animatedProgress by animateFloatAsState(
                                targetValue = progress,
                                animationSpec = tween(durationMillis = 500),
                                label = "Progress Animation"
                            )
                            
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                    
                    // Question text
                    currentQuestion?.let { question ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Text(
                                text = question.text,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Options
                        question.options.forEachIndexed { index, option ->
                            val isSelected = index == selectedOptionIndex
                            val isCorrect = index == question.correctOptionIndex
                            
                            // Different background colors based on state
                            val backgroundColor = when {
                                !showAnswer -> if (isSelected) QuizOptionSelected else QuizOptionUnselected
                                isCorrect -> CorrectAnswer
                                isSelected -> WrongAnswer
                                else -> QuizOptionUnselected
                            }
                            
                            // Ensure text is visible against background
                            val textColor = when {
                                !showAnswer -> if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                isCorrect || (isSelected && !isCorrect) -> Color.White  // White text on colored backgrounds
                                else -> MaterialTheme.colorScheme.onSurface  // Default for unselected options
                            }
                            
                            // Scale animation for selection
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.03f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "Option Scale"
                            )
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .scale(scale)
                                    .clickable(enabled = !showAnswer) {
                                        selectedOptionIndex = index
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = backgroundColor
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 6.dp else 2.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Option letter (A, B, C, D)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A' + index).toString(),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                                    
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = textColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    if (showAnswer) {
                                        if (isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Correct",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Wrong",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Navigation buttons
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + scaleIn()
                        ) {
                            Button(
                                onClick = {
                                    if (showAnswer) {
                                        if (viewModel.isLastQuestion()) {
                                            finalScore = viewModel.getFinalScore()
                                            showResults = true
                                        } else {
                                            viewModel.moveToNextQuestion()
                                            selectedOptionIndex = -1
                                            showAnswer = false
                                        }
                                    } else {
                                        if (selectedOptionIndex >= 0) {
                                            viewModel.submitAnswer(selectedOptionIndex)
                                            showAnswer = true
                                        }
                                    }
                                },
                                enabled = showAnswer || selectedOptionIndex >= 0,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = when {
                                        showAnswer && viewModel.isLastQuestion() -> "Finish Quiz"
                                        showAnswer -> "Next Question"
                                        else -> "Submit Answer"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        
                        // Explanation (when showing answer)
                        AnimatedVisibility(
                            visible = showAnswer && question.explanation.isNotEmpty(),
                            enter = fadeIn(animationSpec = tween(300)) + 
                                    scaleIn(animationSpec = tween(300))
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Explanation",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = question.explanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    onFinish: () -> Unit
) {
    val percentage = if (totalQuestions > 0) {
        (score.toFloat() / totalQuestions.toFloat()) * 100
    } else {
        0f
    }
    
    val isPassing = percentage >= 70
    
    // Background with gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        if (isPassing)
                            CorrectAnswer.copy(alpha = 0.2f)
                        else
                            WrongAnswer.copy(alpha = 0.2f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Trophy icon
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Quiz Result",
                    tint = if (isPassing) CorrectAnswer else WrongAnswer,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isPassing) "Congratulations!" else "Better Luck Next Time!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPassing) CorrectAnswer else WrongAnswer
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Score circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(
                            width = 4.dp,
                            color = if (isPassing) CorrectAnswer else WrongAnswer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$score/$totalQuestions",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${percentage.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isPassing) CorrectAnswer else WrongAnswer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Message
                Text(
                    text = if (isPassing) {
                        "Great job! You've passed the quiz."
                    } else {
                        "Don't worry, you can always try again!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Finish button
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPassing) CorrectAnswer else Primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Finish Quiz",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyQuestionList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No questions available for this quiz",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Exit Quiz?")
        },
        text = {
            Text("Are you sure you want to exit? Your progress will be lost.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Continue Quiz")
            }
        }
    )
}