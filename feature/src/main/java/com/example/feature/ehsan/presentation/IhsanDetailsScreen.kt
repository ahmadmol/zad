package com.example.feature.ehsan.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IhsanDetailsScreen(
    id: Long,
    onBack: () -> Unit,
    viewModel: IhsanDetailsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var reportSent by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        viewModel.loadItem(id)
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(stringResource(R.string.ehsan_report_title)) },
            text = {
                Text(
                    if (reportSent) {
                        stringResource(R.string.ehsan_report_thanks)
                    } else {
                        stringResource(R.string.ehsan_report_confirm_body)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (reportSent) {
                            showReportDialog = false
                            reportSent = false
                        } else {
                            reportSent = true
                        }
                    }
                ) {
                    Text(
                        if (reportSent) stringResource(R.string.common_close)
                        else stringResource(R.string.ehsan_report_confirm)
                    )
                }
            },
            dismissButton = if (!reportSent) {
                {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            } else null
        )
    }

    val colors = IhsanTheme.colors

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.ehsan_details_title), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val item = uiState.item ?: return@IconButton
                                val shareText = context.getString(
                                    R.string.ehsan_share_template,
                                    item.title,
                                    item.description
                                )
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        sendIntent,
                                        context.getString(R.string.ehsan_share_chooser)
                                    )
                                )
                            },
                            modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget)
                        ) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.cd_share)
                            )
                        }
                    }
                )
            },
            containerColor = colors.surfaceMuted
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (uiState.isLoading) {
                    IhsanLoadingState(
                        modifier = Modifier.align(Alignment.Center),
                        message = stringResource(R.string.common_loading)
                    )
                } else if (uiState.errorMessage != null && uiState.item == null) {
                    IhsanErrorState(
                        title = uiState.errorMessage ?: stringResource(R.string.ehsan_unexpected_error),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.item != null) {
                    val item = uiState.item!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Main Image Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .background(colors.surfaceElevated)
                        ) {
                            if (item.imageUrl != null) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(colors.charityOfferContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(item.category) {
                                            "طعام" -> Icons.Default.Restaurant
                                            "ملابس" -> Icons.Default.Checkroom
                                            "أثاث" -> Icons.Default.Weekend
                                            else -> Icons.Default.Category
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                }
                            }
                            
                            Surface(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopEnd),
                                color = if (item.type == "OFFER") IhsanTheme.colors.charityOffer else IhsanTheme.colors.charityRequest,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (item.type == "OFFER") {
                                        stringResource(R.string.ehsan_offer_label)
                                    } else {
                                        stringResource(R.string.ehsan_request_label)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = if (item.type == "OFFER") colors.onSuccess else colors.onWarning,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, size16(), tint = colors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = item.location, color = colors.textSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(Icons.Default.Schedule, contentDescription = null, size16(), tint = colors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "منذ يومين", color = colors.textSecondary, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                stringResource(R.string.ehsan_description_label),
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Contact Info Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = colors.surfaceElevated,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = item.donorName.take(1),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = item.donorName, fontWeight = FontWeight.Bold)
                                        Text(text = stringResource(R.string.ehsan_case_owner), fontSize = 12.sp, color = colors.textSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val phone = item.phoneNumber
                                        val uri = Uri.parse("tel:$phone")
                                        val intent = Intent(Intent.ACTION_DIAL, uri)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(IhsanTheme.dimens.controlHeight),
                                    shape = RoundedCornerShape(IhsanTheme.dimens.radiusMedium),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = stringResource(R.string.cd_call_phone)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.ehsan_call_phone))
                                }
                                
                                Button(
                                    onClick = {
                                        val phone = item.phoneNumber
                                        val url = "https://api.whatsapp.com/send?phone=$phone"
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(url)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(IhsanTheme.dimens.controlHeight),
                                    shape = RoundedCornerShape(IhsanTheme.dimens.radiusMedium),
                                    colors = ButtonDefaults.buttonColors(containerColor = IhsanTheme.colors.whatsapp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = stringResource(R.string.cd_open_whatsapp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.common_whatsapp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = { showReportDialog = true },
                                modifier = Modifier.fillMaxWidth().height(IhsanTheme.dimens.controlHeight),
                                shape = RoundedCornerShape(IhsanTheme.dimens.radiusMedium),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = stringResource(R.string.cd_report_case),
                                    tint = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.ehsan_report_case), color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun size16() = Modifier.size(16.dp)
