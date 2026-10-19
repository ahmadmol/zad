package com.example.feature.profile.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.component.IhsanTopBar
import com.example.designsystem.component.IhsanTopBarStyle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.core.notification.UserMessageNotifier
import com.example.feature.ehsan.data.image.EhsanImageStore
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

val SyrianCities = listOf("حلب", "دمشق", "حمص", "حماة", "اللاذقية", "طرطوس", "إدلب")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val imageStore: EhsanImageStore = koinInject()
    var isCityDropdownExpanded by remember { mutableStateOf(false) }

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
                IhsanTopBar(
                    title = "تعديل الملف الشخصي",
                    style = IhsanTopBarStyle.CompactSkyline,
                    backgroundImageRes = R.drawable.ihsan_mosque_sunrise_landscape,
                    onNavigateBack = onBack
                )
            },
            containerColor = colors.surfaceBase
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding().navigationBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar Section with Camera Icon Overlay
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
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
                        color = darkTealColor.copy(alpha = 0.1f),
                        shadowElevation = 3.dp,
                        border = BorderStroke(2.dp, Color.White)
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
                                Image(
                                    painter = painterResource(id = R.drawable.polish_avatar_placeholder),
                                    contentDescription = "صورة الملف الشخصي",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    // Camera Badge Button Overlay
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
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 3.dp,
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "تغيير الصورة",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                // Form Input Fields (الاسم الكامل, رقم الهاتف, المدينة, نبذة مختصرة عنك)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Field 1: الاسم الكامل
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("الاسم الكامل", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = darkTealColor)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = if (com.example.designsystem.theme.IhsanTheme.isDark) com.example.designsystem.theme.IhsanTheme.colors.accentContent else MaterialTheme.colorScheme.primary,
                            focusedContainerColor = colors.fieldContainer,
                            unfocusedContainerColor = colors.fieldContainer,
                            focusedBorderColor = if (isDark) colors.fieldFocusedBorder else darkTealColor,
                            unfocusedBorderColor = colors.fieldBorder
                        ),
                        singleLine = true
                    )

                    // Field 2: رقم الهاتف
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("رقم الهاتف", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = darkTealColor)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = if (com.example.designsystem.theme.IhsanTheme.isDark) com.example.designsystem.theme.IhsanTheme.colors.accentContent else MaterialTheme.colorScheme.primary,
                            focusedContainerColor = colors.fieldContainer,
                            unfocusedContainerColor = colors.fieldContainer,
                            focusedBorderColor = if (isDark) colors.fieldFocusedBorder else darkTealColor,
                            unfocusedBorderColor = colors.fieldBorder
                        ),
                        singleLine = true
                    )

                    // Field 3: المدينة (with Dropdown Menu)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.city,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المدينة", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = darkTealColor)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isCityDropdownExpanded = !isCityDropdownExpanded }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = darkTealColor)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCityDropdownExpanded = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = if (com.example.designsystem.theme.IhsanTheme.isDark) com.example.designsystem.theme.IhsanTheme.colors.accentContent else MaterialTheme.colorScheme.primary,
                                focusedContainerColor = colors.fieldContainer,
                                unfocusedContainerColor = colors.fieldContainer,
                                focusedBorderColor = if (isDark) colors.fieldFocusedBorder else darkTealColor,
                                unfocusedBorderColor = colors.fieldBorder
                            ),
                            singleLine = true
                        )

                        DropdownMenu(
                            expanded = isCityDropdownExpanded,
                            onDismissRequest = { isCityDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            SyrianCities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city, fontSize = 14.sp) },
                                    onClick = {
                                        viewModel.onCityChange(city)
                                        isCityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Field 4: نبذة مختصرة عنك (Multi-line text area with character counter 51/200)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.bio,
                            onValueChange = viewModel::onBioChange,
                            label = { Text("نبذة مختصرة عنك", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Description, contentDescription = null, tint = darkTealColor)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = if (com.example.designsystem.theme.IhsanTheme.isDark) com.example.designsystem.theme.IhsanTheme.colors.accentContent else MaterialTheme.colorScheme.primary,
                                focusedContainerColor = colors.fieldContainer,
                                unfocusedContainerColor = colors.fieldContainer,
                                focusedBorderColor = if (isDark) colors.fieldFocusedBorder else darkTealColor,
                                unfocusedBorderColor = colors.fieldBorder
                            ),
                            maxLines = 4
                        )

                        // Character Counter Label (e.g. 51/200)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "${uiState.bio.length}/200",
                                fontSize = 12.sp,
                                color = if (isDark) colors.textSecondary else Color(0xFF8A9A97),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons: حفظ التغييرات (Primary) and إلغاء (Outlined)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Primary Action Button: حفظ التغييرات
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
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "حفظ التغييرات",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Secondary Action Button: إلغاء
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, darkTealColor)
                    , colors = com.example.designsystem.theme.ihsanOutlinedButtonColors()) {
                        Text(
                            text = "إلغاء",
                            color = darkTealColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
