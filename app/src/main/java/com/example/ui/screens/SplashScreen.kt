package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    var currentStep by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "splashInfinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(500))
        scale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))

        currentStep = 1
        delay(600)
        currentStep = 2
        delay(600)
        currentStep = 3
        delay(500)

        // Fade out
        alpha.animateTo(0f, animationSpec = tween(300))
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0B1329),
                        DarkBackground
                    ),
                    center = Offset.Unspecified,
                    radius = 1200f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(24.dp)
        ) {
            // Animated Nexus Logo Container
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Glow Wave
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(pulseScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyanPrimary.copy(alpha = 0.35f),
                                    IndigoSecondary.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Rotating Orbital Rings Canvas
                Canvas(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(rotation)
                ) {
                    val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                CyanPrimary,
                                IndigoSecondary,
                                EmeraldTertiary,
                                CyanPrimary
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = stroke
                    )
                }

                // Inner Counter-Rotating Orbit
                Canvas(
                    modifier = Modifier
                        .size(105.dp)
                        .rotate(-rotation * 1.4f)
                ) {
                    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                IndigoSecondary,
                                CyanPrimary,
                                Color.Transparent
                            )
                        ),
                        startAngle = 45f,
                        sweepAngle = 200f,
                        useCenter = false,
                        style = stroke
                    )
                }

                // Central Solid Core Emblem
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(16.dp, CircleShape, spotColor = CyanPrimary, ambientColor = CyanPrimary)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(CyanPrimary, Color(0xFF0284C7), IndigoSecondary)
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H!",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Typography
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HUBIT",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "!",
                    color = CyanPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TV SUPER CHARGED MEDIA SUITE",
                color = CyanPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.5.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Status Indicator Capsule
            val statusMessage = when (currentStep) {
                1 -> stringResource(R.string.splash_status_network)
                2 -> stringResource(R.string.splash_status_optimizing)
                else -> stringResource(R.string.splash_status_ready)
            }

            Box(
                modifier = Modifier
                    .background(DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .border(1.dp, CyanPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (currentStep >= 3) EmeraldTertiary else CyanPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = statusMessage,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
