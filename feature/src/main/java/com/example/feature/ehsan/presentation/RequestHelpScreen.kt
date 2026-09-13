package com.example.feature.ehsan.presentation

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.designsystem.component.EhsanChoiceChipRow
import com.example.designsystem.component.EhsanContactCard
import com.example.designsystem.component.EhsanFieldLabel
import com.example.designsystem.component.EhsanFormCard
import com.example.designsystem.component.EhsanFormSpacing
import com.example.designsystem.component.EhsanImagePicker
import com.example.designsystem.component.EhsanPrimaryAction
import com.example.designsystem.component.EhsanSectionHeader
import com.example.designsystem.component.EhsanSelectorField
import com.example.designsystem.component.EhsanTextField
import com.example.designsystem.component.SegmentedOption
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.notification.UserMessageNotifier
import org.koin.androidx.compose.koinViewModel

/**
 * RequestHelpScreen
 * -----------------
 * Standalone "request help" flow. Visually identical to
 * AddEhsanScreen's REQUEST mode but pre-locked: there is no
 * type toggle, and the title/subtitle are tuned for the
 * ask-for-help mental model.
 *
 * The screen re-uses the same Ehsan* primitives from the
 * design system so the two flows look like one Design System.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestHelpScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEhsanViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(RequestHelpCategories.first().key) }
    var selectedCity by rememberSaveable { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var showCityDropdown by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
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
            selectedCategory = RequestHelpCategories.first().key
            selectedCity = ""
            imageUri = null
            onNavigateBack()
        }
    }

    val colors = IhsanTheme.colors
    val titleError = if (title.isBlank() && uiState.submitAttempted) "الرجاء إدخال عنوان" else null
    val detailsError = if (details.isBlank() && uiState.submitAttempted) "الرجاء إدخال تفاصيل" else null
    val cityError = if (selectedCity.isBlank() && uiState.submitAttempted) "الرجاء اختيار المدينة" else null

    Scaffold(
        containerColor = colors.surfaceBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "طلب مساعدة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "صف احتياجك ليساعدك من يستطيع",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.surfaceBase
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(EhsanFormSpacing.SectionGap)
        ) {
            /* ---- 1. Category ---- */
            EhsanSectionHeader(
                title = "نوع الاحتياج",
                description = "اختر الفئة الأقرب لاحتياجك"
            )
            EhsanChoiceChipRow(
                options = RequestHelpCategories,
                selectedKey = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            /* ---- 2. Image card (with light Liquid Glass) ---- */
            EhsanFormCard(useLiquidGlass = true) {
                EhsanImagePicker(
                    imageUri = imageUri,
                    onPick = { launcher.launch("image/*") },
                    onClear = { imageUri = null },
                    label = "صورة توضيحية",
                    emptyTitle = "أرفق صورة أو إثباتًا للحالة",
                    emptyHint = "اختياري — يساعد المتبرعين على اتخاذ قرار",
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

            /* ---- 3. Details card ---- */
            EhsanFormCard {
                EhsanSectionHeader(
                    title = "تفاصيل الطلب",
                    description = "كلما كان الوضوح أكبر، زادت فرصة الاستجابة"
                )

                Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                EhsanTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "عنوان الطلب",
                    placeholder = "مثال: مساعدة في تكاليف علاج",
                    errorText = titleError,
                    imeAction = ImeAction.Next
                )

                Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                EhsanTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = "تفاصيل الحاجة",
                    placeholder = "اشرح الموقف، الأولوية، الموعد المطلوب…",
                    errorText = detailsError,
                    singleLine = false,
                    minLines = 5,
                    imeAction = ImeAction.Default
                )
            }

            /* ---- 4. Location card ---- */
            EhsanFormCard {
                EhsanSectionHeader(
                    title = "الموقع",
                    description = "سيساعد هذا في توجيه المتبرعين القريبين"
                )

                Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                Box {
                    EhsanSelectorField(
                        value = selectedCity,
                        onClick = { showCityDropdown = true },
                        label = "المدينة",
                        placeholder = "اختر المدينة",
                        leadingIcon = Icons.Default.LocationOn,
                        errorText = cityError
                    )

                    DropdownMenu(
                        expanded = showCityDropdown,
                        onDismissRequest = { showCityDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        RequestHelpCities.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    selectedCity = option.key
                                    showCityDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            /* ---- 5. Contact info ---- */
            EhsanSectionHeader(
                title = "معلومات التواصل",
                description = "سيتم استخدام هذه المعلومات للتواصل معك"
            )
            EhsanContactCard(
                name = uiState.currentUserName,
                phone = uiState.currentUserPhone
            )

            /* ---- 6. Primary action ---- */
            Spacer(Modifier.height(8.dp))
            EhsanPrimaryAction(
                text = "إرسال الطلب",
                onClick = {
                    viewModel.submitRequest(
                        title = title,
                        description = details,
                        category = RequestHelpCategories.first { it.key == selectedCategory }.label,
                        location = RequestHelpCities.first { it.key == selectedCity }.label,
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
}

/* --- Domain option lists --- */

private val RequestHelpCategories = listOf(
    SegmentedOption("طعام", "طعام", Icons.Default.Restaurant),
    SegmentedOption("ملابس", "ملابس", Icons.Default.Checkroom),
    SegmentedOption("أثاث", "أثاث", Icons.Default.Weekend),
    SegmentedOption("مساعدة طبية", "مساعدة طبية", Icons.Default.MedicalServices)
)

private val RequestHelpCities = listOf(
    SegmentedOption("حلب", "حلب، سوريا"),
    SegmentedOption("دمشق", "دمشق، سوريا"),
    SegmentedOption("حمص", "حمص، سوريا"),
    SegmentedOption("اللاذقية", "اللاذقية، سوريا"),
    SegmentedOption("حماة", "حماة، سوريا")
)
