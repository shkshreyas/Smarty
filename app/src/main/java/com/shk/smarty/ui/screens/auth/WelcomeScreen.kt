package com.shk.smarty.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shk.smarty.R
import com.shk.smarty.ui.components.FuturisticButton
import com.shk.smarty.ui.components.GlowingBackground
import com.shk.smarty.ui.theme.GradientEnd
import com.shk.smarty.ui.theme.GradientMid
import com.shk.smarty.ui.theme.GradientStart
import com.shk.smarty.ui.theme.NeonBlue
import com.shk.smarty.ui.theme.NeonCyan
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition()
    
    // Animate logo pulse
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Animate glow
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Animate logo rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Show animations sequentially
    LaunchedEffect(Unit) {
        delay(500)
        showContent = true
        delay(1000)
        showButtons = true
    }
    
    GlowingBackground(
        modifier = Modifier.fillMaxSize(),
        glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        numParticles = 60
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo container with glowing effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonBlue.copy(alpha = glowAlpha),
                                    NeonBlue.copy(alpha = 0f)
                                )
                            ),
                            radius = size.width * scale * 0.6f,
                            center = center
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Animated logo
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(scale)
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                        .clip(CircleShape)
                        .drawWithContent {
                            drawContent()
                            drawCircle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        GradientStart,
                                        GradientMid,
                                        GradientEnd
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                ),
                                style = Stroke(width = 10f)
                            )
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_brain),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Animated text and content
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(1000)) + 
                        slideInVertically(animationSpec = tween(1000)) { it / 2 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SMARTY",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Boost Your Learning Journey",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Quiz. Learn. Master.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(60.dp))
                    
                    // Buttons
                    AnimatedVisibility(
                        visible = showButtons,
                        enter = fadeIn(animationSpec = tween(1000)) + 
                                slideInVertically(animationSpec = tween(1000)) { it / 2 }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Get Started button (Sign Up)
                            FuturisticButton(
                                text = "GET STARTED",
                                onClick = onSignupClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Sign in button
                            OutlinedButton(
                                onClick = onLoginClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(2.dp, NeonBlue),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "SIGN IN",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 