package com.example.feature.ehsan.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.component.CategoryCardOption
import com.example.designsystem.component.EhsanCategoryGrid
import com.example.designsystem.component.EhsanContactCard
import com.example.designsystem.component.EhsanContactMethodSelector
import com.example.designsystem.component.EhsanFormCard
import com.example.designsystem.component.EhsanFormSpacing
import com.example.designsystem.component.EhsanHeroHeader
import com.example.designsystem.component.EhsanImagePicker
import com.example.designsystem.component.EhsanPrimaryAction
import com.example.designsystem.component.EhsanReviewSummaryCard
import com.example.designsystem.component.EhsanSectionHeader
import com.example.designsystem.component.EhsanSelectorField
import com.example.designsystem.component.EhsanStepIndicator
import com.example.designsystem.component.EhsanTextField
import com.example.designsystem.component.StepItem
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.core.notification.UserMessageNotifier
import com.example.feature.core.util.HijriDateFormatter
import org.koin.androidx.compose.koinViewModel

/**
 * AddEhsanScreen — "إضافة عرض تبرع"
 * ----------------------------------------------------
 * 3-stage donation offer creation flow matching reference UI.
 *
 * Stage 1: المعلومات (Category, Title, Sub-category, City, Contact method)
 * Stage 2: التفاصيل (Description, Notes)
 * Stage 3: الصور والمراجعة (Image upload & Review summary, Publish CTA)
 */
@Composable
fun AddEhsanScreen(
    onNavigateBack: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") initialType: String = "OFFER",
    viewModel: AddEhsanViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Stepper state (1, 2, or 3)
    var currentStep by rememberSaveable { mutableIntStateOf(1) }

    // Form inputs
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var categoryKey by rememberSaveable { mutableStateOf(DonationOfferCategories.first().key) }
    var subCategory by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("حلب") }
    var contactMethod by rememberSaveable { mutableStateOf("WHATSAPP") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var showCityDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }

    // Intercept system back button when on step 2 or 3
    BackHandler(enabled = currentStep > 1) {
        currentStep--
    }

    LaunchedEffect(uiState.phoneError) {
        uiState.phoneError?.let {
            UserMessageNotifier.notify(context, it)
            snackbarHostState.showSnackbar(it)
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
            title = ""
            description = ""
            categoryKey = DonationOfferCategories.first().key
            city = "حلب"
            imageUri = null
            onNavigateBack()
        }
    }

    val colors = IhsanTheme.colors

    val titleError = if (title.isBlank() && uiState.submitAttempted) "الرجاء إدخال عنوان العرض" else null
    val cityError = if (city.isBlank() && uiState.submitAttempted) "الرجاء اختيار المدينة" else null
    val descriptionError = if (description.isBlank() && uiState.submitAttempted) "الرجاء إدخال وصف العرض" else null

    Scaffold(
        containerColor = colors.surfaceBase,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Hero Banner Header
            EhsanHeroHeader(
                title = "إضافة عرض تبرع",
                subtitle = "شارك خيرك مع مجتمعك",
                cityName = city.ifBlank { "حلب" },
                islamicDate = HijriDateFormatter.nowFormatted(),
                logoPainter = painterResource(R.drawable.splash_ihsan_logo_transparent),
                onBackClick = {
                    if (currentStep > 1) {
                        currentStep--
                    } else {
                        onNavigateBack()
                    }
                }
            )

            // Form Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(EhsanFormSpacing.SectionGap)
            ) {
                // 2. 3-Step Progress Indicator
                EhsanStepIndicator(
                    steps = listOf(
                        StepItem(1, "المعلومات"),
                        StepItem(2, "التفاصيل"),
                        StepItem(3, "الصور")
                    ),
                    currentStep = currentStep,
                    onStepClick = { targetStep ->
                        if (targetStep < currentStep) {
                            currentStep = targetStep
                        }
                    }
                )

                // 3. Step Content
                when (currentStep) {
                    1 -> {
                        /* ---- Stage 1: المعلومات ---- */

                        // Section: نوع التبرع (Category Cards Grid)
                        EhsanFormCard {
                            EhsanSectionHeader(
                                title = "نوع التبرع",
                                description = "اختر الفئة التي تريد التبرع بها"
                            )
                            Spacer(Modifier.height(12.dp))
                            EhsanCategoryGrid(
                                categories = DonationOfferCategories,
                                selectedKey = categoryKey,
                                onSelect = { categoryKey = it }
                            )
                        }

                        // Section: المعلومات الأساسية (العنوان، التصنيف، المدينة)
                        EhsanFormCard {
                            EhsanSectionHeader(
                                title = "المعلومات الأساسية",
                                description = "أدخل بيانات التبرع الرئيسية"
                            )

                            Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                            // العنوان
                            EhsanTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = "العنوان",
                                placeholder = "أدخل عنوان العرض",
                                leadingIcon = Icons.Default.LocationOn,
                                errorText = titleError,
                                imeAction = ImeAction.Next
                            )

                            Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                            // التصنيف الفرعي
                            Box {
                                EhsanSelectorField(
                                    value = subCategory.ifBlank { categoryKey },
                                    label = "التصنيف",
                                    placeholder = "اختر التصنيف",
                                    onClick = { showCategoryDropdown = true }
                                )

                                DropdownMenu(
                                    expanded = showCategoryDropdown,
                                    onDismissRequest = { showCategoryDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    DonationOfferCategories.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                categoryKey = option.key
                                                subCategory = option.label
                                                showCategoryDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                            // المدينة
                            Box {
                                EhsanSelectorField(
                                    value = city,
                                    label = "المدينة",
                                    placeholder = "اختر المدينة",
                                    onClick = { showCityDropdown = true },
                                    leadingIcon = Icons.Default.LocationOn,
                                    errorText = cityError
                                )

                                DropdownMenu(
                                    expanded = showCityDropdown,
                                    onDismissRequest = { showCityDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    DonationCities.forEach { cityName ->
                                        DropdownMenuItem(
                                            text = { Text(cityName) },
                                            onClick = {
                                                city = cityName
                                                showCityDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Section: وسيلة التواصل
                        EhsanFormCard {
                            EhsanSectionHeader(
                                title = "وسيلة التواصل المفضلة",
                                description = "حدد كيف تود أن يتواصل معك المتلقون"
                            )
                            Spacer(Modifier.height(12.dp))
                            EhsanContactMethodSelector(
                                selectedMethod = contactMethod,
                                onSelect = { contactMethod = it }
                            )
                        }

                        // Primary Action CTA -> Step 2
                        EhsanPrimaryAction(
                            text = "التالي",
                            onClick = {
                                if (title.isNotBlank() && city.isNotBlank()) {
                                    currentStep = 2
                                } else {
                                    UserMessageNotifier.notify(context, "يرجى إكمال الحقول المطلوبة")
                                }
                            }
                        )
                    }

                    2 -> {
                        /* ---- Stage 2: التفاصيل ---- */

                        EhsanFormCard {
                            EhsanSectionHeader(
                                title = "الوصف والتفاصيل",
                                description = "اكتب وصفاً دقيقاً ومفيداً للعنصر المتبرع به"
                            )

                            Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                            EhsanTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = "الوصف",
                                placeholder = "اكتب وصفاً مختصراً ومفيداً...",
                                leadingIcon = Icons.Default.Edit,
                                errorText = descriptionError,
                                singleLine = false,
                                minLines = 5,
                                imeAction = ImeAction.Default
                            )
                        }

                        // Primary Action CTA -> Step 3
                        EhsanPrimaryAction(
                            text = "التالي",
                            onClick = {
                                if (description.isNotBlank()) {
                                    currentStep = 3
                                } else {
                                    UserMessageNotifier.notify(context, "يرجى إدخال وصف العرض")
                                }
                            }
                        )
                    }

                    3 -> {
                        /* ---- Stage 3: الصور والمراجعة ---- */

                        // Image Picker Card
                        EhsanFormCard {
                            EhsanImagePicker(
                                imageUri = imageUri,
                                onPick = { launcher.launch("image/*") },
                                onClear = { imageUri = null },
                                label = "صور العرض",
                                emptyTitle = "أضف صورة للعنصر المتبرع به",
                                emptyHint = "اختياري — الصور تزيد من فرصة التفاعل",
                                imageContent = {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            )
                        }

                        // Review Summary Card
                        EhsanReviewSummaryCard(
                            title = title,
                            category = categoryKey,
                            city = city,
                            description = description,
                            contactPhone = uiState.currentUserPhone.ifBlank { "0912345678" }
                        )

                        // Contact info reminder
                        EhsanContactCard(
                            name = uiState.currentUserName,
                            phone = uiState.currentUserPhone
                        )

                        // Final Publish CTA -> create OFFER
                        EhsanPrimaryAction(
                            text = "نشر العرض",
                            onClick = {
                                viewModel.submitRequest(
                                    title = title,
                                    description = description,
                                    category = categoryKey,
                                    location = city,
                                    type = "OFFER",
                                    imageUrl = imageUri
                                )
                            },
                            enabled = title.isNotBlank() && description.isNotBlank() && city.isNotBlank(),
                            isLoading = uiState.isSubmitting,
                            icon = Icons.AutoMirrored.Filled.Send
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/* --- Domain Category & City Options --- */

private val DonationOfferCategories = listOf(
    CategoryCardOption("ملابس", "ملابس", Icons.Default.Checkroom),
    CategoryCardOption("أثاث", "أثاث", Icons.Default.Weekend),
    CategoryCardOption("مواد غذائية", "مواد غذائية", Icons.Default.Restaurant),
    CategoryCardOption("كتب", "كتب", Icons.AutoMirrored.Filled.MenuBook)
)

private val DonationCities = listOf(
    "حلب",
    "دمشق",
    "حمص",
    "اللاذقية",
    "حماة"
)
