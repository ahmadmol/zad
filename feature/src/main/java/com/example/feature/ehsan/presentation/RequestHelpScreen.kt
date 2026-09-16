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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
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
 * RequestHelpScreen — "طلب مساعدة"
 * ----------------------------------------------------
 * 3-stage help request creation flow matching reference UI.
 *
 * Stage 1: المعلومات (Category, Title, Sub-category, City, Contact method)
 * Stage 2: التفاصيل (Case details, Photo/Proof picker)
 * Stage 3: التواصل والمراجعة (Contact card & Review summary, Submit CTA)
 */
@Composable
fun RequestHelpScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEhsanViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Stepper state (1, 2, or 3)
    var currentStep by rememberSaveable { mutableIntStateOf(1) }

    // Form inputs
    var title by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var selectedCategoryKey by rememberSaveable { mutableStateOf(RequestHelpCategories.first().key) }
    var subCategory by rememberSaveable { mutableStateOf("") }
    var selectedCity by rememberSaveable { mutableStateOf("حلب") }
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
            details = ""
            selectedCategoryKey = RequestHelpCategories.first().key
            selectedCity = "حلب"
            imageUri = null
            onNavigateBack()
        }
    }

    val colors = IhsanTheme.colors

    val titleError = if (title.isBlank() && uiState.submitAttempted) "الرجاء إدخال عنوان الطلب" else null
    val cityError = if (selectedCity.isBlank() && uiState.submitAttempted) "الرجاء اختيار المدينة" else null
    val detailsError = if (details.isBlank() && uiState.submitAttempted) "الرجاء إدخال تفاصيل الحاجة" else null

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
                title = "طلب مساعدة",
                subtitle = "شارك احتياجك ليصلك الدعم المناسب",
                cityName = selectedCity.ifBlank { "حلب" },
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
                        StepItem(3, "التواصل")
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

                        // Section: نوع المساعدة (Category Cards Grid)
                        EhsanFormCard {
                            EhsanSectionHeader(
                                title = "نوع المساعدة",
                                description = "اختر الفئة الأقرب لاحتياجك"
                            )
                            Spacer(Modifier.height(12.dp))
                            EhsanCategoryGrid(
                                categories = RequestHelpCategories,
                                selectedKey = selectedCategoryKey,
                                onSelect = { selectedCategoryKey = it }
                            )
                        }

                        // Section: المعلومات الأساسية (العنوان، الفئة، المدينة)
                        EhsanFormCard {
                            EhsanSectionHeader(
                                title = "المعلومات الأساسية",
                                description = "أدخل عنوان ونوع وموقع الطلب"
                            )

                            Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                            // العنوان
                            EhsanTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = "العنوان",
                                placeholder = "أدخل عنوان الطلب",
                                leadingIcon = Icons.Default.LocationOn,
                                errorText = titleError,
                                imeAction = ImeAction.Next
                            )

                            Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                            // الفئة الفرعية
                            Box {
                                EhsanSelectorField(
                                    value = subCategory.ifBlank { selectedCategoryKey },
                                    label = "الفئة",
                                    placeholder = "اختر الفئة",
                                    onClick = { showCategoryDropdown = true }
                                )

                                DropdownMenu(
                                    expanded = showCategoryDropdown,
                                    onDismissRequest = { showCategoryDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    RequestHelpCategories.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                selectedCategoryKey = option.key
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
                                    value = selectedCity,
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
                                    RequestHelpCities.forEach { cityName ->
                                        DropdownMenuItem(
                                            text = { Text(cityName) },
                                            onClick = {
                                                selectedCity = cityName
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
                                description = "حدد كيف تود أن يتواصل معك المتبرعون"
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
                                if (title.isNotBlank() && selectedCity.isNotBlank()) {
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
                                title = "تفاصيل الحاجة",
                                description = "اشرح حاجتك باختصار ووضوح لمساعدة الآخرين في فهم الحالة"
                            )

                            Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                            EhsanTextField(
                                value = details,
                                onValueChange = { details = it },
                                label = "تفاصيل الطلب",
                                placeholder = "اشرح حاجتك باختصار ووضوح...",
                                leadingIcon = Icons.Default.Edit,
                                errorText = detailsError,
                                singleLine = false,
                                minLines = 5,
                                imeAction = ImeAction.Default
                            )
                        }

                        // Image / Proof Picker Card
                        EhsanFormCard {
                            EhsanImagePicker(
                                imageUri = imageUri,
                                onPick = { launcher.launch("image/*") },
                                onClear = { imageUri = null },
                                label = "صورة توضيحية أو إثبات",
                                emptyTitle = "أرفق صورة توضح الحالة أو الحاجة",
                                emptyHint = "اختياري — يساعد المتبرعين على تقديم الدعم المناسب",
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

                        // Primary Action CTA -> Step 3
                        EhsanPrimaryAction(
                            text = "التالي",
                            onClick = {
                                if (details.isNotBlank()) {
                                    currentStep = 3
                                } else {
                                    UserMessageNotifier.notify(context, "يرجى إدخال تفاصيل الحاجة")
                                }
                            }
                        )
                    }

                    3 -> {
                        /* ---- Stage 3: التواصل والمراجعة ---- */

                        // Contact info Card
                        EhsanContactCard(
                            name = uiState.currentUserName,
                            phone = uiState.currentUserPhone
                        )

                        // Review Summary Card
                        EhsanReviewSummaryCard(
                            title = title,
                            category = selectedCategoryKey,
                            city = selectedCity,
                            description = details,
                            contactPhone = uiState.currentUserPhone.ifBlank { "0912345678" }
                        )

                        // Final Submit CTA -> create REQUEST
                        EhsanPrimaryAction(
                            text = "إرسال طلب المساعدة",
                            onClick = {
                                viewModel.submitRequest(
                                    title = title,
                                    description = details,
                                    category = selectedCategoryKey,
                                    location = selectedCity,
                                    type = "REQUEST",
                                    imageUrl = imageUri
                                )
                            },
                            enabled = title.isNotBlank() && details.isNotBlank() && selectedCity.isNotBlank(),
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

private val RequestHelpCategories = listOf(
    CategoryCardOption("علاج", "علاج", Icons.Default.MedicalServices),
    CategoryCardOption("غذاء", "غذاء", Icons.Default.Restaurant),
    CategoryCardOption("تعليم", "تعليم", Icons.Default.School),
    CategoryCardOption("سكن", "سكن", Icons.Default.Home)
)

private val RequestHelpCities = listOf(
    "حلب",
    "دمشق",
    "حمص",
    "اللاذقية",
    "حماة"
)
