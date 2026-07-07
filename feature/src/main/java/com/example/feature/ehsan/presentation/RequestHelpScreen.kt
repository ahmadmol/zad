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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.designsystem.theme.PrimaryTeal
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestHelpScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEhsanViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("طعام") }
    var selectedCity by remember { mutableStateOf("حلب، سوريا") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var donorName by remember { mutableStateOf("") }
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

    LaunchedEffect(Unit) {
        viewModel.userName.collect { name ->
            donorName = name
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "طلب مساعدة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryTeal)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color.White
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
                        viewModel.addDonation(
                            title = title,
                            description = details,
                            category = selectedCategory,
                            location = selectedCity,
                            type = "REQUEST",
                            donorName = donorName.ifBlank { "مستخدم" },
                            phoneNumber = "0000000000", // Default as it's not in the design yet
                            imageUrl = imageUri
                        )
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    enabled = title.isNotBlank() && details.isNotBlank()
                ) {
                    Text(
                        "إرسال الطلب",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFFFDF2E9) else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                color = if (isSelected) Color(0xFFE67E22) else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFFE67E22) else Color.Gray,
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = Color.Gray
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Black,
            focusedBorderColor = PrimaryTeal,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
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
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(location, color = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE67E22))
            }
        }
    }
}

@Composable
fun FileUploadSection(
    imageUri: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)) 
            .background(Color.White)
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
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "إرفاق الأوراق أو الإثباتات الداعمة\n(اختياري)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class CategoryItem(val name: String, val icon: ImageVector)
