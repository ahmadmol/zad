package com.example.feature.ihsanplus.charitytrust.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.charitytrust.presentation.IhsanPlusCharityTrustViewModel
import com.example.feature.ihsanplus.charitytrust.presentation.components.*
import com.example.feature.ihsanplus.charitytrust.presentation.state.IhsanPlusCharityTrustAction
import com.example.feature.ihsanplus.charitytrust.presentation.state.IhsanPlusCharityTrustUiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IhsanPlusCharityTrustScreen(
    viewModel: IhsanPlusCharityTrustViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("موثوقية إحسان", style = MaterialTheme.typography.titleLarge) }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (val state = uiState) {
                    is IhsanPlusCharityTrustUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is IhsanPlusCharityTrustUiState.Success -> {
                        IhsanPlusCharityTrustContent(
                            data = state.data,
                            onAction = viewModel::onAction
                        )
                    }
                    is IhsanPlusCharityTrustUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IhsanPlusCharityTrustContent(
    data: com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusCharityTrustDashboard,
    onAction: (IhsanPlusCharityTrustAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            IhsanPlusCharityTrustHeader(
                title = data.title,
                subtitle = data.subtitle
            )
        }

        item {
            IhsanPlusHighlightedCaseCard(
                case = data.highlightedCase,
                onClick = { onAction(IhsanPlusCharityTrustAction.OpenCaseDetails(data.highlightedCase.id)) }
            )
        }

        item {
            IhsanPlusTrustSummaryCard(
                summary = data.trustSummary
            )
        }

        item {
            IhsanPlusVerificationChecklist(
                items = data.verificationItems
            )
        }

        item {
            IhsanPlusTransparencyList(
                items = data.transparencyItems
            )
        }

        item {
            IhsanPlusPrivacySafetyCards(
                privacyGuidelines = data.privacyGuidelines,
                safetyGuidelines = data.safetyGuidelines
            )
        }

        item {
            IhsanPlusDonationImpactList(
                items = data.impactItems
            )
        }

        item {
            IhsanPlusCharityTrustQuickActions(
                actions = data.quickActions,
                onActionClick = { onAction(IhsanPlusCharityTrustAction.QuickActionClicked(it)) }
            )
        }

        item {
            Text(
                text = "هذه واجهة تأسيسية معزولة لتحسين الثقة والشفافية في إحسان، ولم يتم ربطها بميزة إحسان الحالية أو قاعدة البيانات بعد.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(IhsanTheme.spacing.large),
                textAlign = TextAlign.Center
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
        }
    }
}
