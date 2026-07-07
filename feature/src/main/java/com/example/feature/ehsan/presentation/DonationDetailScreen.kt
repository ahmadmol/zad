package com.example.feature.ehsan.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationDetailScreen(
    donationId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DonationDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(donationId) {
        viewModel.loadDonation(donationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل الإحسان") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        val donation = uiState.donation
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (donation != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFFF1F3F4)),
                    contentAlignment = Alignment.Center
                ) {
                    if (donation.imageUrl != null) {
                        AsyncImage(
                            model = donation.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val icon = when (donation.category) {
                            "أثاث" -> Icons.Default.Weekend
                            "طعام" -> Icons.Default.Restaurant
                            "ملابس" -> Icons.Default.Checkroom
                            "أجهزة" -> Icons.Default.Devices
                            else -> Icons.Default.Category
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    }
                    
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(IhsanTheme.spacing.medium),
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = donation.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Column(modifier = Modifier.padding(IhsanTheme.spacing.large)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = donation.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = if (donation.type == "OFFER") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (donation.type == "OFFER") "عرض تبرع" else "طلب مساعدة",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (donation.type == "OFFER") Color(0xFF2E7D32) else Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Text(donation.location, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(IhsanTheme.spacing.medium))
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Text("نشط حالياً", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
                    Text("الوصف", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
                    Text(
                        donation.description,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, color = Color.DarkGray)
                    )

                    Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
                    
                    // Donor Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(IhsanTheme.spacing.medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(donation.donorName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(IhsanTheme.spacing.small))
                            Column {
                                Text(donation.donorName, fontWeight = FontWeight.Bold)
                                Text("عضو موثق", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(IhsanTheme.spacing.large))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(IhsanTheme.spacing.small)
                    ) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تواصل")
                        }
                        
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اتصال", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(IhsanTheme.spacing.medium))
                }
            }
        }
    }
}
