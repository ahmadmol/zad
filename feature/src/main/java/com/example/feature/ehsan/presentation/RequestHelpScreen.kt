package com.example.feature.ehsan.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.notification.UserMessageNotifier
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestHelpScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEhsanViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("طعام") }
    var selectedCity by remember { mutableStateOf("حلب، سوريا") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var showCityDropdown by remember { mutableStateOf(false) }

    val categories = listOf(
        CategoryItem("طعام", Icons.Default.Restaurant),
        CategoryItem("ملابس", Icons.Default.Checkroom),
        CategoryItem("أثاث", Icons.Default.Weekend),
        CategoryItem("مساعدة طبية", Icons.Default.MedicalServices)
    )

    val cities = listOf("حلب، سوريا", "دمشق، سوريا", "حمص، سوريا", "اللاذقية، سوريا", "حماة، سوريا")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }

    LaunchedEffect(uiState.phoneError) {
        uiState.phoneError?.let {
            UserMessageNotifier.notify(context, it)
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(uiState.submissionError) {
        uiState.submissionError?.let {
            UserMessageNotifier.notify(context, it)
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            onNavigateBack()
        }
    }

    val colors = IhsanTheme.colors

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "طلب مساعدة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.brand
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.brand)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.onBrand
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.surface
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Categories Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            item = category,
                            isSelected = selectedCategory == category.name,
                            onClick = { selectedCategory = category.name }
                        )
                    }
                }

                // Title Input
                EhsanRequestTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "عنوان الطلب"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Details Input
                EhsanRequestTextField(
                    value = details,
                    onValueChange = { details = it },
                    placeholder = "تفاصيل الحاجة",
                    minLines = 6,
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location Selector
                Box {
                    LocationSelector(
                        location = selectedCity,
                        onClick = { showCityDropdown = true }
                    )
                    DropdownMenu(
                        expanded = showCityDropdown,
                        onDismissRequest = { showCityDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedCity = city
                                    showCityDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Upload Section
                FileUploadSection(
                    imageUri = imageUri,
                    onClick = { launcher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Submit Button
                Button(
                    onClick = {
                        viewModel.submitRequest(
                            title = title,
                            description = details,
                            category = selectedCategory,
                            location = selectedCity,
                            type = "REQUEST",
                            imageUrl = imageUri
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.brand),
                    enabled = title.isNotBlank() && details.isNotBlank() && !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.onBrand,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "إرسال الطلب",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onBrand
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CategoryChip(
    item: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = IhsanTheme.colors

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) colors.charityRequestContainer else colors.surfaceElevated,
        border = if (isSelected) null else BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                color = if (isSelected) colors.charityRequest else colors.textSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) colors.charityRequest else colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EhsanRequestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
    singleLine: Boolean = true
) {
    val fieldColors = IhsanTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = fieldColors.textSecondary
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = fieldColors.fieldBorder,
            focusedBorderColor = fieldColors.fieldFocusedBorder,
            unfocusedContainerColor = fieldColors.fieldContainer,
            focusedContainerColor = fieldColors.fieldContainer,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        minLines = minLines,
        singleLine = singleLine
    )
}

@Composable
fun LocationSelector(
    location: String,
    onClick: () -> Unit
) {
    val colors = IhsanTheme.colors

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.fieldBorder),
        color = colors.fieldContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(location, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.charityRequest)
            }
        }
    }
}

@Composable
fun FileUploadSection(
    imageUri: String?,
    onClick: () -> Unit
) {
    val colors = IhsanTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(1.dp, colors.fieldBorder, RoundedCornerShape(16.dp))
            .background(colors.fieldContainer)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.charityOfferContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        tint = colors.brand,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "إرفاق الأوراق أو الإثباتات الداعمة\n(اختياري)",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class CategoryItem(val name: String, val icon: ImageVector)
