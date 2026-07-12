package com.example.feature.ihsanplus.prayerassist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusCompassCalibrationTip

@Composable
fun IhsanPlusCompassCalibrationTipsList(
    tips: List<IhsanPlusCompassCalibrationTip>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium)
    ) {
        Text(
            text = "نصائح لمعايرة البوصلة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = IhsanTheme.spacing.small)
        )
        tips.forEachIndexed { index, tip ->
            TipItem(index + 1, tip)
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
        }
    }
}

@Composable
private fun TipItem(number: Int, tip: IhsanPlusCompassCalibrationTip) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = IhsanTheme.spacing.small)
        )
        Column {
            Text(text = tip.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = tip.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
