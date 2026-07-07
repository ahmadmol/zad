package com.example.feature.prayer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.prayer.presentation.PrayerAction
import com.example.feature.prayer.presentation.PrayerViewModel
import org.koin.androidx.compose.koinViewModel

data class PrayerTime(
    val nameAr: String,
    val nameEn: String,
    val time: String,
    val timestamp: Long = 0,
    val isPast: Boolean = false,
    val isActive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "إحسان",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Menu */ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color(0xFFF8F9FA)
        ) { padding ->
            if (uiState.isLoading && uiState.prayerTimes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "أوقات الصلاة",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = uiState.locationName,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Timeline vertical line
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 12.dp)
                                .width(1.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f))
                                .align(Alignment.CenterStart)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(uiState.prayerTimes) { prayer ->
                                val isNext = prayer.nameAr == uiState.nextPrayerName
                                PrayerTimelineItem(
                                    prayer = prayer,
                                    countdown = if (isNext) uiState.nextPrayerCountdown else null,
                                    location = uiState.locationName
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        uiState.currentDate,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        uiState.hijriDate.ifBlank { HijriDateFormatter.nowFormatted() },
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerTimelineItem(prayer: PrayerTime, countdown: String?, location: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .border(2.dp, if (countdown != null) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape)
                .background(if (countdown != null) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Card content
        Box(modifier = Modifier.weight(1f)) {
            if (countdown != null) {
                ActivePrayerCard(prayer, countdown, location)
            } else {
                StandardPrayerCard(prayer)
            }
        }
    }
}

@Composable
fun StandardPrayerCard(prayer: PrayerTime) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (prayer.isPast) Color.White.copy(alpha = 0.6f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (prayer.isPast) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = prayer.nameAr,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = prayer.nameEn.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
            }
            
            Text(
                text = prayer.time,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (prayer.isPast) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
            )

            Icon(
                imageVector = if (prayer.isPast) Icons.Default.NotificationsOff else Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = if (prayer.isPast) Color.LightGray else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ActivePrayerCard(prayer: PrayerTime, countdown: String, location: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = prayer.nameAr,
                            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = prayer.nameEn.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                        )
                    }
                    
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null, 
                            tint = Color(0xFFFF8A65), 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "الوقت المتبقي",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f), letterSpacing = 2.sp)
                )
                
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color(0xFFFF8A65),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f))
                        )
                    }

                    Surface(
                        color = Color(0xFFFF8A65),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "جاري الآن",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
