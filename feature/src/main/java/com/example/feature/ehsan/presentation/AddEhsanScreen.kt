package com.example.feature.ehsan.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEhsanScreen(
    onNavigateBack: () -> Unit = {},
    initialType: String = "OFFER",
    viewModel: AddEhsanViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("طعام") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(initialType) }
    var donorName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    val categories = listOf("طعام", "ملابس", "أثاث", "أجهزة", "أخرى")
    val cities = listOf("حلب", "دمشق", "حمص", "اللاذقية", "حماة")
    var showCityDropdown by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }

    LaunchedEffect(Unit) {
        viewModel.userName.collect { name ->
            if (donorName.isBlank()) donorName = name
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AddEhsanEvent.Success -> onNavigateBack()
                is AddEhsanEvent.Error -> {
                    // Show toast or snackbar
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (type == "OFFER") "إضافة تبرع جديد" else "إضافة طلب مساعدة", 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(IhsanTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(IhsanTheme.spacing.medium)
        ) {
            // Image Picker Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F3F4))
                    .clickable { launcher.launch("image/*") },
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("إضافة صورة للغرض", color = Color.Gray)
                    }
                }
            }

            // Type Selection
            Text("نوع الطلب", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = type == "OFFER",
                    onClick = { type = "OFFER" },
                    label = { Text("عرض تبرع") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = type == "REQUEST",
                    onClick = { type = "REQUEST" },
                    label = { Text("طلب مساعدة") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (type == "OFFER") "عنوان التبرع" else "عنوان المساعدة المطلوبة") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("الوصف") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Text("التصنيف", fontWeight = FontWeight.Bold)
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(category),
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Location Selection
            Text("المدينة", fontWeight = FontWeight.Bold)
            Box {
                OutlinedTextField(
                    value = if (location.isBlank()) "اختر المدينة..." else location,
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCityDropdown = true },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (location.isBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { showCityDropdown = true })
                
                DropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                location = city
                                showCityDropdown = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()
            Text("معلومات التواصل", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = donorName,
                onValueChange = { donorName = it },
                label = { Text("اسم المتبرع") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("رقم الجوال") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.addDonation(
                        title, description, category, location, type, donorName, phoneNumber, imageUri
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تأكيد الإضافة", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
