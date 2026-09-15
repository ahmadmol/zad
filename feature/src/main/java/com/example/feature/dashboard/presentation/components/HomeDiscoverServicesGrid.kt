package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

data class ServiceItemData(
    val title: String,
    val icon: ImageVector? = null,
    val drawableResId: Int? = null,
    val route: String
)

@Composable
fun HomeDiscoverServicesGrid(
    onServiceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val services = listOf(
        ServiceItemData("القرآن", drawableResId = R.drawable.ihsan_icon_quran, route = "quran"),
        ServiceItemData("الصلاة", drawableResId = R.drawable.ihsan_icon_prayer, route = "prayer"),
        ServiceItemData("القبلة", drawableResId = R.drawable.ihsan_icon_qibla, route = "qibla"),
        ServiceItemData("أسماء الله", icon = Icons.Default.AutoAwesome, route = "asma"),
        ServiceItemData("الأدعية", icon = Icons.Default.VolunteerActivism, route = "dua"),
        ServiceItemData("التسبيح", icon = Icons.Default.BrightnessLow, route = "tasbih"),
        ServiceItemData("الأذكار", icon = Icons.Default.SelfImprovement, route = "azkar")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_services_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.home_services_subtitle),
                    fontSize = 11.sp,
                    color = IhsanTheme.colors.textSecondaryMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 columns x 2 rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                services.take(3).forEach { service ->
                    ServiceTileCard(
                        service = service,
                        onClick = { onServiceClick(service.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                services.drop(3).take(3).forEach { service ->
                    ServiceTileCard(
                        service = service,
                        onClick = { onServiceClick(service.route) },
                        modifier = Modifier.weight(1f)
                    )
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
    Card(
        modifier = modifier
            .height(48.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = service.title
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (service.drawableResId != null) {
                Image(
                    painter = painterResource(id = service.drawableResId),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            } else if (service.icon != null) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = service.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
