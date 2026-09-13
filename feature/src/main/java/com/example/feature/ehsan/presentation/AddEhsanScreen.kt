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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.designsystem.component.EhsanChoiceChipRow
import com.example.designsystem.component.EhsanContactCard
import com.example.designsystem.component.EhsanFieldLabel
import com.example.designsystem.component.EhsanFormCard
import com.example.designsystem.component.EhsanFormSpacing
import com.example.designsystem.component.EhsanImagePicker
import com.example.designsystem.component.EhsanPrimaryAction
import com.example.designsystem.component.EhsanSectionHeader
import com.example.designsystem.component.EhsanSegmentedToggle
import com.example.designsystem.component.EhsanSelectorField
import com.example.designsystem.component.EhsanTextField
import com.example.designsystem.component.SegmentedOption
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.notification.UserMessageNotifier
import org.koin.androidx.compose.koinViewModel

/**
 * AddEhsanScreen
 * ---------------
 * Single source of truth for both "Add Donation" (OFFER) and
 * "Add Help Request" (REQUEST) flows. The type is chosen by the
 * user via the segmented toggle at the top of the form.
 *
 * UI/UX goals (M3 + restrained Liquid Glass):
 *   • One calm title that adapts to the chosen type.
 *   • Sections are wrapped in soft white cards with a 1px
 *     hairline border. The first card (Image) uses a subtle
 *     Liquid Glass treatment for a hint of depth.
 *   • Generous but consistent vertical rhythm.
 *   • Every interactive surface is ≥ 48dp tall.
 *   • RTL-friendly paddings only — no Left/Right asymmetry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEhsanScreen(
    onNavigateBack: () -> Unit = {},
    initialType: String = "OFFER",
    viewModel: AddEhsanViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Form state — kept local; the ViewModel is only used for
    // submission side-effects and current user data.
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(DonationCategories.first().key) }
    var city by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(initialType) }
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
            description = ""
            category = DonationCategories.first().key
            city = ""
            imageUri = null
            onNavigateBack()
        }
    }

    val colors = IhsanTheme.colors
    val isOffer = type == "OFFER"
    val screenTitle = if (isOffer) "إضافة تبرع جديد" else "إضافة طلب مساعدة"
    val screenSubtitle = if (isOffer) {
        "اعرض ما تريد التبرع به لمن يحتاجه"
    } else {
        "اطلب المساعدة التي تحتاجها من المتبرعين"
    }

    val titleError = if (title.isBlank() && uiState.submitAttempted) "الرجاء إدخال عنوان" else null
    val descriptionError = if (description.isBlank() && uiState.submitAttempted) "الرجاء إدخال وصف" else null
    val cityError = if (city.isBlank() && uiState.submitAttempted) "الرجاء اختيار المدينة" else null

    Scaffold(
        containerColor = colors.surfaceBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = screenTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = screenSubtitle,
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
            /* ---- 1. Type chooser (segmented) ---- */
            EhsanSectionHeader(
                title = "نوع الطلب",
                description = "اختر إذا كنت تعرض تبرعًا أو تطلب مساعدة"
            )
            EhsanSegmentedToggle(
                options = listOf(
                    SegmentedOption(
                        key = "OFFER",
                        label = "تبرع",
                        icon = Icons.Filled.VolunteerActivism
                    ),
                    SegmentedOption(
                        key = "REQUEST",
                        label = "طلب مساعدة",
                        icon = Icons.Filled.Restaurant
                    )
                ),
                selectedKey = type,
                onSelect = { type = it }
            )

            /* ---- 2. Image card (with light Liquid Glass) ---- */
            EhsanFormCard(useLiquidGlass = true) {
                EhsanImagePicker(
                    imageUri = imageUri,
                    onPick = { launcher.launch("image/*") },
                    onClear = { imageUri = null },
                    label = if (isOffer) "صورة الغرض" else "صورة توضيحية",
                    emptyTitle = if (isOffer) "أضف صورة للعنصر المتبرع به" else "أضف صورة توضح الحالة",
                    emptyHint = "اختياري — يساعد الآخرين على فهم الاحتياج",
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
                    title = if (isOffer) "تفاصيل التبرع" else "تفاصيل الاحتياج",
                    description = "اكتب وصفًا واضحًا يساعد الآخرين على الفهم"
                )

                Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                EhsanTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = if (isOffer) "عنوان التبرع" else "عنوان الاحتياج",
                    placeholder = if (isOffer) "مثال: أجهزة كهربائية بحالة جيدة" else "مثال: مساعدة في إيجار المنزل",
                    errorText = titleError,
                    imeAction = ImeAction.Next
                )

                Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                EhsanTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "الوصف",
                    placeholder = "اشرح التفاصيل، الكمية، الحالة، إلخ…",
                    errorText = descriptionError,
                    singleLine = false,
                    minLines = 4,
                    imeAction = ImeAction.Default
                )
            }

            /* ---- 4. Category + City card ---- */
            EhsanFormCard {
                EhsanSectionHeader(
                    title = "التصنيف والموقع",
                    description = "ساعد الآخرين في إيجاد طلبك بسرعة"
                )

                Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                EhsanFieldLabel("التصنيف")
                Spacer(Modifier.height(8.dp))
                EhsanChoiceChipRow(
                    options = DonationCategories,
                    selectedKey = category,
                    onSelect = { category = it }
                )

                Spacer(Modifier.height(EhsanFormSpacing.FieldGap))

                Box {
                    EhsanSelectorField(
                        value = city,
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
                        DonationCities.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    city = option.key
                                    showCityDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            /* ---- 5. Contact info card (read-only reminder) ---- */
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
                text = "تأكيد الإضافة",
                onClick = {
                    viewModel.submitRequest(
                        title = title,
                        description = description,
                        category = DonationCategories.first { it.key == category }.label,
                        location = DonationCities.first { it.key == city }.label,
                        type = type,
                        imageUrl = imageUri
                    )
                },
                enabled = title.isNotBlank() && description.isNotBlank() && city.isNotBlank(),
                isLoading = uiState.isSubmitting,
                icon = Icons.AutoMirrored.Filled.Send
            )
        }
    }
}

/* --- Domain-specific option lists --- */

private val DonationCategories = listOf(
    SegmentedOption("طعام", "طعام", Icons.Default.Restaurant),
    SegmentedOption("ملابس", "ملابس", Icons.Default.Checkroom),
    SegmentedOption("أثاث", "أثاث", Icons.Default.Weekend),
    SegmentedOption("أجهزة", "أجهزة", Icons.Default.Devices),
    SegmentedOption("أخرى", "أخرى", Icons.Default.MoreHoriz)
)

private val DonationCities = listOf(
    SegmentedOption("حلب", "حلب"),
    SegmentedOption("دمشق", "دمشق"),
    SegmentedOption("حمص", "حمص"),
    SegmentedOption("اللاذقية", "اللاذقية"),
    SegmentedOption("حماة", "حماة")
)
