package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

data class ServiceItemData(
    val title: String,
    val drawableResId: Int,
    val route: String
)

@Composable
fun HomeDiscoverServicesGrid(
    onServiceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTealColor = Color(0xFF003B46)

    val services = listOf(
        ServiceItemData("القرآن", drawableResId = R.drawable.ihsan_icon_quran, route = "quran"),
        ServiceItemData("الصلاة", drawableResId = R.drawable.ihsan_icon_prayer, route = "prayer"),
        ServiceItemData("القبلة", drawableResId = R.drawable.ihsan_icon_qibla, route = "qibla"),
        ServiceItemData("الأذكار", drawableResId = R.drawable.ic_azkar_morning, route = "azkar"),
        ServiceItemData("التسبيح", drawableResId = R.drawable.ic_search_tasbih, route = "tasbih"),
        ServiceItemData("الأدعية", drawableResId = R.drawable.ic_search_dua, route = "dua"),
        ServiceItemData("الحديث", drawableResId = R.drawable.ic_search_hadith, route = "hadith"),
        ServiceItemData("الأسماء الحسنى", drawableResId = R.drawable.ic_search_asma, route = "asma"),
        ServiceItemData("البث المباشر", drawableResId = R.drawable.ihsan_live_mosque_sunset, route = "live_chooser"),
        ServiceItemData("التبرع", drawableResId = R.drawable.ic_search_ehsan, route = "donations"),
        ServiceItemData("جدول الأعمال", drawableResId = R.drawable.ic_azkar_all, route = "daily"),
        ServiceItemData("الإحصائيات", drawableResId = R.drawable.ic_azkar_various, route = "statistics"),
        ServiceItemData("البحث", drawableResId = R.drawable.ihsan_icon_search, route = "search"),
        ServiceItemData("فهم القرآن", drawableResId = R.drawable.ic_search_quran, route = "fahm"),
        ServiceItemData("نبي الإحسان", drawableResId = R.drawable.ic_azkar_after_prayer, route = "nabi_ihsan"),
        ServiceItemData("السنة والسيرة", drawableResId = R.drawable.ic_azkar_sleep, route = "sanhya")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header matching Image 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        tint = darkTealColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الخدمات",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = darkTealColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "كل ما تحتاجه في مكان واحد",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = IhsanTheme.colors.textSecondaryMuted,
                    modifier = Modifier.padding(start = 26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            services.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { service ->
                        ServiceTileCard(
                            service = service,
                            onClick = { onServiceClick(service.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceTileCard(
    service: ServiceItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTealColor = Color(0xFF003B46)

    Card(
        modifier = modifier
            .height(58.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = service.title
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEBF5F3)
        ),
        border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = service.drawableResId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = service.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkTealColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = darkTealColor.copy(alpha = 0.70f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
