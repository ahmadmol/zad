package com.example.feature.azkar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.MolTheme

// --- Data Contracts ---

/**
 * Immutable state representing the UI of the Azkar screen.
 */
data class AzkarUiState(
    val currentZikr: String = "",
    val counter: Int = 0
)

/**
 * Actions that can be triggered from the Azkar UI.
 */
sealed interface AzkarAction {
    object OnIncrement : AzkarAction
    object OnReset : AzkarAction
}

// --- Screen Wrapper ---

/**
 * Entry point for the Azkar Screen.
 * This wrapper is responsible for passing state and handling actions.
 */
@Composable
fun AzkarScreen(
    state: AzkarUiState,
    onAction: (AzkarAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AzkarContent(
        state = state,
        onIncrement = { onAction(AzkarAction.OnIncrement) },
        onReset = { onAction(AzkarAction.OnReset) },
        modifier = modifier
    )
}

// --- Content Composable (Stateless) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarContent(
    state: AzkarUiState,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "الأذكار",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(MolTheme.spacing.medium)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Zikr Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MolTheme.spacing.large),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = state.currentZikr,
                    modifier = Modifier
                        .padding(MolTheme.spacing.large)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
            }

            // Counter Display
            Text(
                text = state.counter.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 80.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = MolTheme.spacing.extraLarge)
            )

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MolTheme.spacing.medium)
            ) {
                Button(
                    onClick = onIncrement,
                    modifier = Modifier.weight(2f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(MolTheme.spacing.medium)
                ) {
                    Text(
                        text = "تسبيح",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(MolTheme.spacing.medium)
                ) {
                    Text(
                        text = "تصفير",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "Normal State", locale = "ar")
@Composable
fun AzkarScreenPreview() {
    MolTheme {
        AzkarScreen(
            state = AzkarUiState(
                currentZikr = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                counter = 33
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Counter at Zero", locale = "ar")
@Composable
fun AzkarScreenZeroPreview() {
    MolTheme {
        AzkarScreen(
            state = AzkarUiState(
                currentZikr = "الحمد لله",
                counter = 0
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Large Counter", locale = "ar")
@Composable
fun AzkarScreenLargePreview() {
    MolTheme {
        AzkarScreen(
            state = AzkarUiState(
                currentZikr = "الله أكبر",
                counter = 999
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Long Zikr Text", locale = "ar")
@Composable
fun AzkarScreenLongTextPreview() {
    MolTheme {
        AzkarScreen(
            state = AzkarUiState(
                currentZikr = "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                counter = 10
            ),
            onAction = {}
        )
    }
}
