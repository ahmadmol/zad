package com.example.feature.azkar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.designsystem.theme.*
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.azkar.domain.usecase.IncrementCounterUseCase
import com.example.feature.azkar.domain.usecase.ResetCounterUseCase
import com.example.feature.azkar.presentation.AzkarAction
import com.example.feature.azkar.presentation.AzkarUiState
import com.example.feature.azkar.presentation.AzkarViewModel
import com.example.feature.azkar.presentation.AzkarViewModelFactory

@Composable
fun AzkarScreen(
    getAzkarUseCase: GetAzkarUseCase,
    incrementCounterUseCase: IncrementCounterUseCase,
    resetCounterUseCase: ResetCounterUseCase
) {
    val viewModel: AzkarViewModel = viewModel(
        factory = AzkarViewModelFactory(
            getAzkarUseCase = getAzkarUseCase,
            incrementCounterUseCase = incrementCounterUseCase,
            resetCounterUseCase = resetCounterUseCase
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AzkarContent(
            state = uiState,
            onAction = viewModel::onAction
        )
    }
}

@Composable
private fun AzkarContent(
    state: AzkarUiState,
    onAction: (AzkarAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (state.error != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "خطأ: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(MolTheme.spacing.medium))
                Button(onClick = { /* ViewModel should handle refresh if needed */ }) {
                    Text("إعادة المحاولة")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MolTheme.spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // App Title
                Text(
                    text = "بركة",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Zikr Text Section
                Text(
                    text = state.zikertext,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MolTheme.spacing.medium)
                )

                // Counter Circle Section
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(280.dp)
                ) {
                    val progress by animateFloatAsState(
                        targetValue = if (state.targetCount > 0) state.currentCount.toFloat() / state.targetCount else 0f,
                        label = "progress"
                    )
                    
                    val isCompleted = state.currentCount >= state.targetCount && state.targetCount > 0
                    val activeColor = if (isCompleted) Accent else MaterialTheme.colorScheme.primary

                    // Progress Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Grey.copy(alpha = 0.1f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = activeColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Gradient Circle
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .shadow(16.dp, CircleShape)
                            .background(
                                brush = if (isCompleted) 
                                    Brush.linearGradient(listOf(Accent, Accent)) 
                                    else Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.currentCount.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 64.sp,
                                    color = White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "/${state.targetCount}",
                                style = MaterialTheme.typography.titleLarge.copy(color = White.copy(alpha = 0.8f))
                            )
                        }
                    }
                }

                // Action Buttons Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MolTheme.spacing.medium),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = { onAction(AzkarAction.OnReset) },
                        modifier = Modifier
                            .height(64.dp)
                            .weight(1f),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(MolTheme.spacing.small))
                        Text("إعادة", color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(MolTheme.spacing.medium))

                    // Increment Button
                    Button(
                        onClick = { onAction(AzkarAction.OnIncrement) },
                        modifier = Modifier
                            .height(64.dp)
                            .weight(2f),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = White
                        )
                        Spacer(modifier = Modifier.width(MolTheme.spacing.small))
                        Text("تسبيح", style = MaterialTheme.typography.labelLarge.copy(color = White))
                    }
                }
            }
        }
    }
}

// Previews
@Preview(showBackground = true, name = "Normal State", locale = "ar")
@Composable
fun AzkarContentNormalPreview() {
    MolTheme {
        AzkarContent(
            state = AzkarUiState(
                zikertext = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                currentCount = 10,
                targetCount = 33,
                isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Completed State", locale = "ar")
@Composable
fun AzkarContentCompletedPreview() {
    MolTheme {
        AzkarContent(
            state = AzkarUiState(
                zikertext = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                currentCount = 33,
                targetCount = 33,
                isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State", locale = "ar")
@Composable
fun AzkarContentLoadingPreview() {
    MolTheme {
        AzkarContent(
            state = AzkarUiState(isLoading = true),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State", locale = "ar")
@Composable
fun AzkarContentErrorPreview() {
    MolTheme {
        AzkarContent(
            state = AzkarUiState(error = "فشل في جلب البيانات", isLoading = false),
            onAction = {}
        )
    }
}
