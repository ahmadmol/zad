package com.example.feature.ehsan.presentation

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
import com.example.feature.core.notification.UserMessageNotifier
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.domain.model.DonationStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IhsanDetailsScreen(
    id: Long,
    onBack: () -> Unit,
    viewModel: IhsanDetailsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val imageStore: EhsanImageStore = koinInject()

    LaunchedEffect(id) {
        viewModel.loadItem(id)
    }

    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark
    val darkTealColor = if (isDark) colors.textPrimary else Color(0xFF003B46)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                val item = uiState.item

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        modifier = Modifier.matchParentSize()
                    )

                    if (isDark) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.55f))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .semantics {
                                            role = Role.Button
                                            contentDescription = context.getString(R.string.cd_back)
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.cd_back),
                                        tint = darkTealColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = when (item?.type) {
                                        "OFFER" -> "تفاصيل عرض تبرع"
                                        "REQUEST" -> "تفاصيل طلب مساعدة"
                                        else -> stringResource(R.string.ehsan_details_title)
                                    },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = darkTealColor
                                )
                            }

                            if (item != null) {
                                IconButton(
                                    onClick = {
                                        val shareText = context.getString(
                                            R.string.ehsan_share_template,
                                            item.title,
                                            item.description
                                        )
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        runCatching {
                                            context.startActivity(
                                                Intent.createChooser(
                                                    sendIntent,
                                                    context.getString(R.string.ehsan_share_chooser)
                                                )
                                            )
                                        }.onFailure { UserMessageNotifier.notify(context, "تعذر فتح تطبيق للمشاركة") }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = stringResource(R.string.cd_share),
                                        tint = darkTealColor
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = colors.surfaceBase
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
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
                    val statusTyped = DonationStatus.fromStorage(item.status)
                    val isOffer = item.type == "OFFER"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        // Main Content Image Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = colors.surfaceElevated,
                            border = BorderStroke(1.dp, colors.borderSubtle)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val imageModel = imageStore.resolve(item.imageUrl)
                                if (imageModel != null) {
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = item.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(if (isOffer) colors.charityOfferContainer else colors.charityRequestContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (item.category) {
                                                "طعام" -> Icons.Default.Restaurant
                                                "ملابس" -> Icons.Default.Checkroom
                                                "أثاث" -> Icons.Default.Weekend
                                                else -> if (isOffer) Icons.Default.VolunteerActivism else Icons.Default.Handshake
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp),
                                            tint = if (isOffer) colors.charityOffer.copy(alpha = 0.4f) else colors.charityRequest.copy(alpha = 0.4f)
                                        )
                                    }
                                }

                                // Type Badge Overlay
                                Surface(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .align(Alignment.TopEnd),
                                    color = if (isOffer) colors.charityOfferContainer else colors.charityRequestContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isOffer) colors.charityOffer.copy(alpha = 0.5f) else colors.charityRequest.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isOffer) Icons.Default.VolunteerActivism else Icons.Default.Handshake,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isOffer) colors.charityOffer else colors.charityRequest
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isOffer) "عرض تبرع" else "طلب مساعدة",
                                            color = if (isOffer) colors.charityOffer else colors.charityRequest,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Title
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status & Location & Time Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Status Chip (Real status only)
                            val (statusBg, statusFg, statusText) = when (statusTyped) {
                                DonationStatus.ACTIVE -> Triple(colors.charityOfferContainer, colors.charityOffer, "نشط")
                                DonationStatus.COORDINATING -> Triple(colors.surfaceMint, MaterialTheme.colorScheme.primary, "جارٍ التنسيق")
                                DonationStatus.FULFILLED -> Triple(colors.charityOfferContainer, colors.success, "مكتمل")
                                DonationStatus.EXPIRED -> Triple(colors.surfaceMuted, colors.textSecondary, "منتهي")
                                DonationStatus.CANCELLED -> Triple(colors.charityRequestContainer, colors.charityRequest, "ملغى")
                            }

                            Surface(
                                color = statusBg,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusFg)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = statusText,
                                        color = statusFg,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (item.location.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = item.location, color = colors.textSecondary, fontSize = 13.sp)
                                }
                            }

                            if (item.createdAt > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = DateUtils.getRelativeTimeSpanString(
                                            item.createdAt,
                                            System.currentTimeMillis(),
                                            DateUtils.MINUTE_IN_MILLIS
                                        ).toString(),
                                        color = colors.textSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Section 1: Detailed Description
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = colors.surfaceElevated,
                            border = BorderStroke(1.dp, colors.borderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isOffer) Icons.Default.Info else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isOffer) "عن هذا العرض" else "تفاصيل الحاجة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = colors.textPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary,
                                    lineHeight = 24.sp
                                )

                                if (item.category.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Surface(
                                        color = colors.surfaceMuted,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "الفئة: ${item.category}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Contact / Owner Card
                        if (item.donorName.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = colors.surfaceElevated,
                                border = BorderStroke(1.dp, colors.borderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(46.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = item.donorName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = if (isOffer) "صاحب العرض" else "صاحب الطلب",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // CTA Action Buttons
                        if (item.phoneNumber.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val phone = item.phoneNumber
                                        val uri = Uri.parse("tel:$phone")
                                        val intent = Intent(Intent.ACTION_DIAL, uri)
                                        runCatching { context.startActivity(intent) }
                                            .onFailure { UserMessageNotifier.notify(context, "تعذر فتح تطبيق الاتصال") }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = stringResource(R.string.cd_call_phone),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تواصل", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }

                                Button(
                                    onClick = {
                                        val phone = item.phoneNumber
                                        val url = "https://api.whatsapp.com/send?phone=$phone"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        runCatching { context.startActivity(intent) }
                                            .onFailure { UserMessageNotifier.notify(context, "تعذر فتح واتساب") }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.whatsapp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = stringResource(R.string.cd_open_whatsapp),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.common_whatsapp), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

