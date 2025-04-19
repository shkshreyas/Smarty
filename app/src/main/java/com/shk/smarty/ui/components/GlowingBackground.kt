package com.shk.smarty.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val speed: Float,
    val direction: Int // 1 for up, -1 for down
)

@Composable
fun GlowingBackground(
    modifier: Modifier = Modifier,
    glowColor: Color = Color(0xFF007AFF).copy(alpha = 0.3f),
    particleColor: Color = Color.White.copy(alpha = 0.4f),
    numParticles: Int = 40,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Animate gradient rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Animate pulsing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Create particles
    val particles = remember {
        List(numParticles) {
            Particle(
                x = Random.nextFloat() * 1f,
                y = Random.nextFloat() * 1f,
                radius = Random.nextFloat() * 6f + 2f,
                alpha = Random.nextFloat() * 0.2f + 0.1f,
                speed = Random.nextFloat() * 0.001f + 0.0005f,
                direction = if (Random.nextBoolean()) 1 else -1
            )
        }
    }
    
    // Update particle positions
    val particlePositions = remember { mutableStateOf(particles) }
    
    // Create gradient colors for background
    val backgroundColors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
        MaterialTheme.colorScheme.background
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.radialGradient(backgroundColors))
    ) {
        // Particles canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            val updatedParticles = particlePositions.value.map { particle ->
                // Update y position
                var newY = particle.y + (particle.speed * particle.direction)
                
                // If particle goes off screen, reset it
                if (newY > 1f) newY = 0f
                if (newY < 0f) newY = 1f
                
                // Draw the particle
                drawParticle(
                    x = particle.x * size.width,
                    y = newY * size.height,
                    radius = particle.radius,
                    color = particleColor.copy(alpha = particle.alpha)
                )
                
                // Return updated particle
                particle.copy(y = newY)
            }
            
            // Update particle state
            particlePositions.value = updatedParticles
            
            // Draw glow effects
            drawCircle(
                color = glowColor,
                radius = size.minDimension * 0.5f * pulseScale,
                center = Offset(size.width * 0.5f, size.height * 0.15f)
            )
            
            drawCircle(
                color = glowColor.copy(alpha = 0.3f),
                radius = size.minDimension * 0.3f * pulseScale,
                center = Offset(size.width * 0.8f, size.height * 0.7f)
            )
        }
        
        // Content
        content()
    }
}

private fun DrawScope.drawParticle(
    x: Float,
    y: Float,
    radius: Float,
    color: Color
) {
    drawCircle(
        color = color,
        radius = radius,
        center = Offset(x, y)
    )
} 