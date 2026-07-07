package com.example.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.components.AuthBottomSheet
import com.example.feature.profile.presentation.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onNavigateToDonationHistory: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAuthSheet by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "الملف الشخصي",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color(0xFFF9F9F9)
        ) { padding ->
            if (showAuthSheet) {
                AuthBottomSheet(
                    onDismiss = { showAuthSheet = false },
                    onAuthSuccess = { showAuthSheet = false }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!uiState.isUserLoggedIn) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = Color(0xFFF5F5F5)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).padding(24.dp),
                                    tint = Color.LightGray
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "سجل دخولك لتتمكن من إدارة تبرعاتك ومتابعة تأثيرك",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = { showAuthSheet = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4D3D))
                            ) {
                                Text("تسجيل الدخول / إنشاء حساب", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Profile Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Surface(
                                    modifier = Modifier.size(100.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF0D4D3D)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = uiState.userName.take(1),
                                            color = Color.White,
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Surface(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .border(2.dp, Color.White, CircleShape)
                                        .clickable { onEditProfileClick() },
                                    shape = CircleShape,
                                    color = Color(0xFF6B9080)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.userName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.userPhone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    // Impact Card
                    ImpactCard(
                        donationsCount = uiState.myDonations.count { it.type == "OFFER" },
                        requestsCount = uiState.myDonations.count { it.type == "REQUEST" }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Menu Section
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.History,
                                title = "سجل التبرعات",
                                onClick = onNavigateToDonationHistory
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))
                            ProfileMenuItem(
                                icon = Icons.Default.Notifications,
                                title = "إعدادات التنبيهات",
                                onClick = { /* TODO */ }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))
                            ProfileMenuItem(
                                icon = Icons.Default.Language,
                                title = "اللغة",
                                subtitle = "العربية",
                                onClick = { /* TODO */ }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))
                            ProfileMenuItem(
                                icon = Icons.Default.Shield,
                                title = "الخصوصية والأمان",
                                onClick = { /* TODO */ }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Support Section
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.Chat,
                                title = "مركز المساعدة",
                                onClick = { /* TODO */ }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                title = "تسجيل الخروج",
                                textColor = Color(0xFFE57373),
                                showChevron = false,
                                onClick = { viewModel.logout() }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "إصدار التطبيق 1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ImpactCard(donationsCount: Int, requestsCount: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0D4D3D)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("مستوى التأثير", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text("محسن متميز", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatItem(label = "تبرع", value = donationsCount.toString())
                StatItem(label = "طلب", value = requestsCount.toString())
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    textColor: Color = Color.Black,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF9F9F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (textColor != Color.Black) textColor else Color(0xFF0D4D3D),
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
