package com.example.feature.profile.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.notification.UserMessageNotifier
import com.example.feature.ehsan.data.image.EhsanImageStore
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val imageStore: EhsanImageStore = koinInject()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onAvatarSelected(it) }
    }

    val resolvedAvatarModel = remember(uiState.avatarUrl) {
        imageStore.resolve(uiState.avatarUrl)
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            UserMessageNotifier.notify(context, it)
            viewModel.clearMessages()
            onBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            UserMessageNotifier.notify(context, it)
            viewModel.clearMessages()
        }
    }

    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark
    val darkTealColor = if (isDark) colors.textPrimary else Color(0xFF003B46)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                ) {
                    Image(
                        painter = painterResource(id = com.example.feature.R.drawable.ihsan_home_hero_background),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        modifier = Modifier.matchParentSize()
                    )

                    if (isDark) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.55f))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(48.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "رجوع"
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = darkTealColor
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "تعديل الملف الشخصي",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkTealColor
                            )
                        }
                    }
                }
            },
            containerColor = colors.surfaceBase
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Section with Camera Icon Overlay
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (resolvedAvatarModel != null) {
                                AsyncImage(
                                    model = resolvedAvatarModel,
                                    contentDescription = "صورة الملف الشخصي",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = uiState.name.take(1).ifBlank { "م" },
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Camera Icon Badge Overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(34.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = CircleShape,
                        color = colors.surfaceElevated,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.5.dp, colors.borderSubtle)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "تغيير الصورة",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Form Input Fields (matching UserEntity: firstName, lastName, phoneNumber, city, address)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Full Name (combining firstName & lastName)
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("الاسم الكامل") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = colors.textSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.fieldContainer,
                            unfocusedContainerColor = colors.fieldContainer,
                            focusedBorderColor = colors.fieldFocusedBorder,
                            unfocusedBorderColor = colors.fieldBorder
                        ),
                        singleLine = true
                    )

                    // Phone Number (Read-only, general placeholder)
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = {},
                        label = { Text("رقم الهاتف") },
                        placeholder = { Text("رقم الهاتف غير متاح") },
                        supportingText = { Text("رقم الهاتف غير قابل للتعديل", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = colors.textSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surfaceMuted,
                            unfocusedContainerColor = colors.surfaceMuted,
                            focusedBorderColor = colors.borderSubtle,
                            unfocusedBorderColor = colors.borderSubtle
                        ),
                        singleLine = true
                    )

                    // City
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = viewModel::onCityChange,
                        label = { Text("المدينة") },
                        placeholder = { Text("أدخل مدينتك") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.textSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.fieldContainer,
                            unfocusedContainerColor = colors.fieldContainer,
                            focusedBorderColor = colors.fieldFocusedBorder,
                            unfocusedBorderColor = colors.fieldBorder
                        ),
                        singleLine = true
                    )

                    // Address
                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = viewModel::onAddressChange,
                        label = { Text("العنوان بالتفصيل") },
                        placeholder = { Text("أدخل عنوانك بالتفصيل") },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = colors.textSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.fieldContainer,
                            unfocusedContainerColor = colors.fieldContainer,
                            focusedBorderColor = colors.fieldFocusedBorder,
                            unfocusedBorderColor = colors.fieldBorder
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Save CTA Button (54dp height, Deep Teal, spinner on loading)
                Button(
                    onClick = viewModel::saveChanges,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "حفظ التغييرات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cancel / Ignore Action Button
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "تجاهل التغييرات",
                        color = colors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

