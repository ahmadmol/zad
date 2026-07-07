package com.example.feature.ehsan.presentation

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IhsanDetailsScreen(
    id: Long,
    onBack: () -> Unit,
    viewModel: IhsanDetailsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(id) {
        viewModel.loadItem(id)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("تفاصيل الإحسان", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Share Logic */ }) {
                            Icon(Icons.Outlined.Share, contentDescription = null)
                        }
                    }
                )
            },
            containerColor = Color(0xFFF9F9F9)
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF0D4D3D))
                } else if (uiState.item != null) {
                    val item = uiState.item!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Main Image Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .background(Color.White)
                        ) {
                            if (item.imageUrl != null) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(item.category) {
                                            "طعام" -> Icons.Default.Restaurant
                                            "ملابس" -> Icons.Default.Checkroom
                                            "أثاث" -> Icons.Default.Weekend
                                            else -> Icons.Default.Category
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = Color(0xFF0D4D3D).copy(alpha = 0.2f)
                                    )
                                }
                            }
                            
                            Surface(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopEnd),
                                color = if (item.type == "OFFER") Color(0xFF2E7D32) else Color(0xFFE65100),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (item.type == "OFFER") "تبرع متاح" else "طلب مساعدة",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, size16(), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = item.location, color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(Icons.Default.Schedule, contentDescription = null, size16(), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "منذ يومين", color = Color.Gray, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text("وصف الحالة", fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Contact Info Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F3F4))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        color = Color(0xFF0D4D3D).copy(alpha = 0.1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = item.donorName.take(1),
                                                color = Color(0xFF0D4D3D),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = item.donorName, fontWeight = FontWeight.Bold)
                                        Text(text = "صاحب الفرصة", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val phone = item.phoneNumber
                                        val uri = Uri.parse("tel:$phone")
                                        val intent = Intent(Intent.ACTION_DIAL, uri)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4D3D))
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("اتصال هاتفي")
                                }
                                
                                Button(
                                    onClick = {
                                        val phone = item.phoneNumber
                                        val url = "https://api.whatsapp.com/send?phone=$phone"
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(url)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("واتساب")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = { /* TODO: Report condition */ },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("الإبلاغ عن الحالة", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun size16() = Modifier.size(16.dp)
