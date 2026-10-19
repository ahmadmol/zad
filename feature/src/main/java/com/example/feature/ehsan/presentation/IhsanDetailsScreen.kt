package com.example.feature.ehsan.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.notification.UserMessageNotifier
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.domain.model.DonationStatus
import com.example.feature.ehsan.domain.usecase.donationContactDestination
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.DateFormat
import java.util.Date

@Composable
fun IhsanDetailsScreen(id: Long, onBack: () -> Unit, viewModel: IhsanDetailsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val store: EhsanImageStore = koinInject()
    val context = LocalContext.current
    val colors = IhsanTheme.colors
    LaunchedEffect(id) { viewModel.loadItem(id) }
    fun share() {
        val item = state.item ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${item.title}\n${item.description}")
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "مشاركة")) }
            .onFailure { UserMessageNotifier.notify(context, "تعذر المشاركة") }
    }
    Scaffold(containerColor = colors.surfaceBase, contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = { IhsanTopBar(if (state.item?.type == "REQUEST") "تفاصيل طلب المساعدة" else "تفاصيل عرض التبرع",
            style = IhsanTopBarStyle.CompactSkyline, onNavigateBack = onBack,
            actions = { IconButton(onClick = { share() }, enabled = state.item != null) {
                Icon(Icons.Default.Share, "مشاركة", tint = colors.accentContent)
            } }) }) { padding ->
        when {
            state.isLoading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                IhsanLoadingState(message = "جارٍ تحميل التفاصيل")
            }
            state.item == null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                IhsanErrorState("تعذر تحميل التفاصيل", message = state.errorMessage,
                    retryLabel = "إعادة المحاولة", onRetry = { viewModel.loadItem(id) })
            }
            else -> {
                val item = state.item!!
                val offer = item.type == "OFFER"
                val status = when (DonationStatus.fromStorage(item.status)) {
                    DonationStatus.ACTIVE -> "نشط"
                    DonationStatus.COORDINATING -> "جارٍ التنسيق"
                    DonationStatus.FULFILLED -> "مكتمل"
                    DonationStatus.CANCELLED -> "ملغى"
                    DonationStatus.EXPIRED -> "منتهي"
                }
                Column(Modifier.padding(padding).fillMaxSize().navigationBarsPadding()
                    .verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IhsanImageGallery(item.imageUrl, item.title, store)
                    IhsanStatusChip(if (offer) "عرض تبرع" else "طلب مساعدة",
                        container = if (offer) colors.charityOfferContainer else colors.charityRequestContainer,
                        content = if (offer) colors.charityOffer else colors.charityRequest)
                    Text(item.title, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = colors.accentContent)
                    Text(item.description.take(120), fontSize = 14.sp, lineHeight = 21.sp, color = colors.textBody)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IhsanStatusChip(status)
                        Column(Modifier.weight(1f)) {
                            Text(item.location, fontSize = 12.sp, color = colors.textSecondary)
                            if (item.createdAt > 0) Text(DateFormat.getDateInstance(DateFormat.MEDIUM, java.util.Locale.forLanguageTag("ar")).format(Date(item.createdAt)),
                                fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }
                    DetailSection(if (offer) "عن هذا العرض" else "تفاصيل المساعدة المطلوبة", item.description)
                    DetailSection("معلومات التواصل", listOf(item.donorName, item.category).filter { it.isNotBlank() }.joinToString(" • "))
                    val contactDestination = donationContactDestination(item.phoneNumber, item.contactMethod)
                    Button(onClick = {
                        contactDestination?.let { destination ->
                            val action = if (item.contactMethod == "WHATSAPP") Intent.ACTION_VIEW else Intent.ACTION_DIAL
                            runCatching { context.startActivity(Intent(action, Uri.parse(destination))) }
                                .onFailure { UserMessageNotifier.notify(context, "تعذر فتح تطبيق التواصل") }
                        }
                    }, enabled = contactDestination != null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Text(if (offer) "تواصل لتنسيق التبرع" else "تواصل لتقديم المساعدة")
                    }
                    OutlinedButton(colors = com.example.designsystem.theme.ihsanOutlinedButtonColors(), onClick = { share() }, modifier = Modifier.fillMaxWidth()) { Text("مشاركة") }
                    if (item.phoneNumber.isBlank()) Text(if (item.origin == "DEMO") "مثال DEMO خيالي؛ التواصل غير متاح ولا توجد جهة حقيقية" else "لا يتوفر رقم تواصل لهذا السجل", color = colors.textSecondary, fontSize = 12.sp)
                    else if (contactDestination == null) Text("رقم التواصل غير صالح لهذه الطريقة؛ واتساب يتطلب رقمًا دوليًا يبدأ بـ +", color = colors.textSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, text: String) {
    Surface(color = IhsanTheme.colors.surfaceElevated, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = IhsanTheme.colors.accentContent)
            Text(text, fontSize = 14.sp, lineHeight = 22.sp, color = IhsanTheme.colors.textBody)
        }
    }
}
