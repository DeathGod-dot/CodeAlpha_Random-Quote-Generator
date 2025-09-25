package com.example.yumelines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.abs
import kotlin.math.sign
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yumelines.ui.theme.*
import com.example.yumelines.viewmodel.QuoteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YumeLinesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuoteGeneratorScreen()
                }
            }
        }
    }
}

@Composable
fun QuoteGeneratorScreen(
    viewModel: QuoteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    // Gesture and interaction states
    var isFavorite by remember { mutableStateOf(false) }
    var cardOffset by remember { mutableStateOf(Offset.Zero) }
    var cardRotation by remember { mutableStateOf(0f) }
    var isCardPressed by remember { mutableStateOf(false) }
    var pullRefreshOffset by remember { mutableStateOf(0f) }
    
    // Animated values for gestures
    val cardOffsetX by animateFloatAsState(
        targetValue = cardOffset.x,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardOffsetX"
    )
    
    val cardRotationAnimated by animateFloatAsState(
        targetValue = cardRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardRotation"
    )
    
    val cardPressScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardPressScale"
    )
    
    val pullRefreshScale by animateFloatAsState(
        targetValue = 1f + (pullRefreshOffset * 0.001f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "pullRefreshScale"
    )
    
    // Enhanced gradient background with gesture influence
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            GradientStart.copy(alpha = 0.1f + abs(cardOffset.x) * 0.0001f),
            GradientEnd.copy(alpha = 0.1f + abs(cardOffset.y) * 0.0001f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        start = Offset(cardOffset.x + animatedOffset * 1000f, cardOffset.y),
        end = Offset(1000f - cardOffset.x, (1f - animatedOffset) * 1000f + cardOffset.y)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDragEnd = {
                        // Reset pull refresh if not triggered
                        if (pullRefreshOffset < 200f) {
                            pullRefreshOffset = 0f
                        }
                        cardOffset = Offset.Zero
                        cardRotation = 0f
                    }
                ) { change, dragAmount ->
                    val newOffset = cardOffset + dragAmount
                    
                    // Pull to refresh detection (downward drag from top)
                    if (newOffset.y > 0 && cardOffset.y < 100f) {
                        pullRefreshOffset = newOffset.y
                        if (pullRefreshOffset > 200f && !uiState.isLoading) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.fetchRandomQuote()
                            pullRefreshOffset = 0f
                        }
                    }
                    
                    // Card gesture influence
                    cardOffset = newOffset
                    cardRotation = (newOffset.x / 20f).coerceIn(-15f, 15f)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer {
                    scaleX = pullRefreshScale
                    scaleY = pullRefreshScale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Enhanced Animated App Title with gesture response
            val titleScale by animateFloatAsState(
                targetValue = if (uiState.isLoading) 0.95f else 1f + (pullRefreshOffset * 0.001f),
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "titleScale"
            )
            
            val titleRotation by animateFloatAsState(
                targetValue = cardRotationAnimated * 0.3f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "titleRotation"
            )
            
            Text(
                text = "✨ YumeLines ✨",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                        rotationZ = titleRotation
                    }
            )
            
            Text(
                text = if (pullRefreshOffset > 100f) "🔄 Release to refresh!" else "Random Anime Quote Generator",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (pullRefreshOffset > 100f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .graphicsLayer {
                        alpha = if (pullRefreshOffset > 50f) 1f else 0.7f + (pullRefreshOffset * 0.006f)
                    }
            )
        
            // Enhanced Interactive Quote Card with advanced gestures
            val cardScale by animateFloatAsState(
                targetValue = if (uiState.isLoading) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "cardScale"
            )
            
            val cardAlpha by animateFloatAsState(
                targetValue = if (uiState.quote != null) 1f else 0.7f,
                animationSpec = tween(500),
                label = "cardAlpha"
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        scaleX = cardScale * cardPressScale
                        scaleY = cardScale * cardPressScale
                        rotationZ = cardRotationAnimated
                        translationX = cardOffsetX * 0.5f
                        alpha = cardAlpha
                        shadowElevation = (12 + abs(cardOffsetX) * 0.1f).dp.toPx()
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isCardPressed = true
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                tryAwaitRelease()
                                isCardPressed = false
                            },
                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!uiState.isLoading) {
                                    viewModel.fetchRandomQuote()
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (abs(cardOffset.x) > 300f) {
                                    // Swipe threshold reached - fetch new quote
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!uiState.isLoading) {
                                        viewModel.fetchRandomQuote()
                                    }
                                }
                                cardOffset = Offset.Zero
                                cardRotation = 0f
                            }
                        ) { _, dragAmount ->
                            cardOffset = cardOffset.copy(x = cardOffset.x + dragAmount)
                            cardRotation = (cardOffset.x / 20f).coerceIn(-15f, 15f)
                            
                            // Haptic feedback at swipe threshold
                            if (abs(cardOffset.x) > 300f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = (12 + abs(cardOffsetX) * 0.1f).dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = (2 + abs(cardOffsetX) * 0.01f).dp,
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.3f + abs(cardOffsetX) * 0.002f
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val errorMessage = uiState.errorMessage
    
                    when {
                        uiState.isLoading -> {
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing)
                                ),
                                label = "loading"
                            )
                            
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Loading",
                                modifier = Modifier
                                    .size(56.dp)
                                    .rotate(rotation),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "✨ Fetching magical quote... ✨",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    
                        errorMessage != null -> {
                            Text(
                                text = "💔 Oops!",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = errorMessage,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    
                        uiState.quote != null -> {
                            val quote = uiState.quote!!
                            
                            // Animated quote appearance
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(800)) + 
                                       slideInVertically(animationSpec = tween(800)) { it / 2 }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Quote text with gradient
                                    Text(
                                        text = "\"${quote.quote}\"",
                                        fontSize = 20.sp,
                                        fontStyle = FontStyle.Italic,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 20.dp),
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    // Character name with accent
                                    Text(
                                        text = "— ${quote.character} 💫",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    
                                    // Show name with emoji
                                    Text(
                                        text = "🎌 ${quote.show}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    // Action buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Favorite button
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                isFavorite = !isFavorite
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFavorite) CrimsonRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        
                                        // Share button
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                // TODO: Implement share functionality
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Enhanced New Quote Button with floating animation
            val buttonScale by animateFloatAsState(
                targetValue = if (uiState.isLoading) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "buttonScale"
            )
            
            val buttonFloat by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "buttonFloat"
            )
            
            var buttonPressed by remember { mutableStateOf(false) }
            
            val buttonPressScale by animateFloatAsState(
                targetValue = if (buttonPressed) 0.95f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "buttonPressScale"
            )
            
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.fetchRandomQuote()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(64.dp)
                    .graphicsLayer {
                        scaleX = buttonScale * buttonPressScale
                        scaleY = buttonScale * buttonPressScale
                        translationY = buttonFloat
                        shadowElevation = (8 + abs(buttonFloat)).dp.toPx()
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                buttonPressed = true
                                tryAwaitRelease()
                                buttonPressed = false
                            }
                        )
                    },
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = (8 + abs(buttonFloat)).dp,
                    pressedElevation = 16.dp
                ),
                enabled = !uiState.isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (uiState.isLoading) {
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing)
                            ),
                            label = "buttonLoading"
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Loading",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(rotation),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        Text(
                            text = "✨",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (uiState.isLoading) "Summoning Quote..." else "Get New Quote",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!uiState.isLoading) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✨",
                            fontSize = 20.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Enhanced info text with gesture hints
            val infoText = when {
                abs(cardOffset.x) > 200f -> "🔄 Keep swiping to get new quote!"
                pullRefreshOffset > 50f -> "🔄 Pull down to refresh!"
                else -> "💡 Tap, swipe, or pull to get magical quotes!"
            }
            
            Text(
                text = infoText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(0.8f)
                    .graphicsLayer {
                        scaleX = 1f + abs(cardOffset.x) * 0.001f
                        scaleY = 1f + abs(cardOffset.x) * 0.001f
                    }
            )
        }
        
        // Gesture indicators
        if (abs(cardOffset.x) > 100f) {
            Box(
                modifier = Modifier
                    .align(if (cardOffset.x > 0) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(32.dp)
                    .alpha((abs(cardOffset.x) - 100f) / 200f)
            ) {
                Text(
                    text = if (cardOffset.x > 0) "➡️" else "⬅️",
                    fontSize = 32.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = 1f + abs(cardOffset.x) * 0.002f
                            scaleY = 1f + abs(cardOffset.x) * 0.002f
                        }
                )
            }
        }
        
        // Pull to refresh indicator
        if (pullRefreshOffset > 50f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp)
                    .alpha((pullRefreshOffset - 50f) / 150f)
            ) {
                Text(
                    text = "🔄",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = pullRefreshOffset * 2f
                            scaleX = 1f + pullRefreshOffset * 0.005f
                            scaleY = 1f + pullRefreshOffset * 0.005f
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuoteGeneratorScreenPreview() {
    YumeLinesTheme {
        QuoteGeneratorScreen()
    }
}
