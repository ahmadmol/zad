package com.example.feature.quran.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.quran.domain.model.Verse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirBottomSheet(
    verse: Verse,
    surahName: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "$surahName - آية ${verse.verseNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = verse.text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "التفسير",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = verse.tafsir ?: "تفسير هذه الآية غير متوفر حالياً. سيتم إضافته قريباً (TODO).",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Justify
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
