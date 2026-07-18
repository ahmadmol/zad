package com.example.feature.ehsan.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Honest local-board capability notice. No verification or trust claims.
 */
@Composable
fun LocalCharityBoardNotice(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "لوحة إحسان المحلية",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "القوائم محفوظة على جهازك فقط. لا يوجد تحقق رسمي من الحالات، ولا مدفوعات داخل التطبيق، ولا ضمان لتسليم التبرعات عبر المنصة. تواصل مباشرة عبر الهاتف أو واتساب بحدر.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
