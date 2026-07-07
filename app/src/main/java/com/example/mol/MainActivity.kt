package com.example.mol

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.azkar.presentation.AzkarViewModel
import com.example.mol.ui.MainScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val viewModel: AzkarViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            
            IhsanTheme(darkTheme = uiState.isDarkMode) {
                val locationPermissionState = rememberPermissionState(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (locationPermissionState.status.isGranted) {
                        MainScreen()
                    } else {
                        LocationPermissionScreen(
                            shouldShowRationale = locationPermissionState.status.shouldShowRationale,
                            onRequestPermission = { 
                                locationPermissionState.launchPermissionRequest() 
                            },
                            onOpenSettings = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionScreen(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFD32F2F).copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "تفعيل خدمة الموقع",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF146C7A)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (shouldShowRationale) {
                "نحتاج للوصول إلى موقعك لتوفير مواقيت صلاة دقيقة وتحديد اتجاه القبلة لك بناءً على موقعك الحالي في حلب."
            } else {
                "يرجى السماح بالوصول إلى الموقع لتفعيل ميزات (مواقيت الصلاة، القبلة، والتبرعات القريبة منك)."
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            lineHeight = 26.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF146C7A))
        ) {
            Text("منح الإذن الآن", fontWeight = FontWeight.Bold)
        }
        
        if (!shouldShowRationale) {
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("فتح الإعدادات يدوياً", color = Color.Gray)
            }
        }
    }
}