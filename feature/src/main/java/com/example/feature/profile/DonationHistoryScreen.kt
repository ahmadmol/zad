package com.example.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.profile.presentation.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

enum class HistoryFilterTab(val label: String) { ALL("الكل"), OFFERS("عروض التبرع"), REQUESTS("طلبات المساعدة") }

@Composable
fun DonationHistoryScreen(onBack: () -> Unit, onItemClick: (Long) -> Unit, viewModel: ProfileViewModel = koinViewModel()) {
    OwnedListingsContent("سجل المساهمات المحلية", null, false, onBack, onItemClick, viewModel)
}

@Composable
fun ManageListingsScreen(type: String, onBack: () -> Unit, onItemClick: (Long) -> Unit, viewModel: ProfileViewModel = koinViewModel()) {
    OwnedListingsContent(if (type == "REQUEST") "طلباتي" else "عروضي", type, true, onBack, onItemClick, viewModel)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OwnedListingsContent(title: String, type: String?, manage: Boolean, onBack: () -> Unit, onItemClick: (Long) -> Unit, vm: ProfileViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<Donation?>(null) }
    var deleting by remember { mutableStateOf<Donation?>(null) }
    val rows = state.myDonations.filter { (type == null || it.type == type) && (tab == 0 || it.type == if (tab == 1) "OFFER" else "REQUEST") }
    Scaffold(containerColor = IhsanTheme.colors.surfaceBase, contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = { IhsanTopBar(title, style = IhsanTopBarStyle.CompactSkyline, onNavigateBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text("حالات محلية على هذا الجهاز، لا تثبت تنفيذ تبرع أو دفع. السجلات القديمة بلا معرّف مالك لا تنسب إلى ملفك.",
                Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
            if (type == null) FlowRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryFilterTab.entries.forEachIndexed { index, item -> FilterChip(selected = tab == index, onClick = { tab = index }, label = { Text(item.label) }) }
            }
            state.managementError?.let { Text(it, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error) }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                rows.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    IhsanEmptyState(title = if (state.isUserLoggedIn) "لا توجد سجلات لك بعد" else "افتح ملفك المحلي أولًا",
                        message = "ستظهر هنا العروض والطلبات التي تحفظها من هذا الملف الشخصي. أمثلة DEMO ليست مساهماتك.", primaryActionLabel = "رجوع", onPrimaryAction = onBack)
                }
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(rows, key = { it.id }) { row ->
                        Card(colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceElevated)) {
                            IhsanActionCard(title = row.title, subtitle = "${row.location} • ${statusLabel(row.status)} • ${if (row.type == "OFFER") "عرض" else "طلب"}",
                                icon = Icons.Default.Favorite, onClick = { onItemClick(row.id) }, modifier = Modifier.fillMaxWidth())
                            if (manage) Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { editing = row }, enabled = !state.isManaging) { Text("تعديل") }
                                TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { vm.updateStatus(row.id, row.status != "COMPLETED") }, enabled = !state.isManaging) { Text(if (row.status == "COMPLETED") "إعادة إتاحة" else "إنهاء محلي") }
                                TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { deleting = row }, enabled = !state.isManaging) { Text("حذف", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }
    editing?.let { row -> EditListingDialog(row, state.isManaging, state.managementError, { editing = null }) { edited, images -> vm.editListing(edited, images) { editing = null } } }
    deleting?.let { row -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("حذف السجل المحلي؟") },
        text = { Text("سيحذف «${row.title}» وصوره الخاصة غير المشتركة من هذا الجهاز. لا يمكن التراجع.") },
        confirmButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { vm.deleteDonation(row); deleting = null }) { Text("حذف") } },
        dismissButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { deleting = null }) { Text("إلغاء") } }) }
}

private fun statusLabel(status: String) = when (status) { "COMPLETED" -> "منتهٍ محليًا"; "PENDING" -> "بانتظار محلي"; else -> "متاح محليًا" }

@Composable
private fun EditListingDialog(row: Donation, saving: Boolean, error: String?, onDismiss: () -> Unit, onSave: (Donation, List<String>?) -> Unit) {
    var title by rememberSaveable(row.id) { mutableStateOf(row.title) }
    var description by rememberSaveable(row.id) { mutableStateOf(row.description) }
    var category by rememberSaveable(row.id) { mutableStateOf(row.category) }
    var location by rememberSaveable(row.id) { mutableStateOf(row.location) }
    var status by rememberSaveable(row.id) { mutableStateOf(row.status) }
    var expanded by remember { mutableStateOf(false) }
    var selectedImages by rememberSaveable(row.id) { mutableStateOf<ArrayList<String>?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val imageStore = remember(context) { com.example.feature.ehsan.data.image.EhsanImageStore(context) }
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris -> if (uris.isNotEmpty()) selectedImages = ArrayList(uris.map { it.toString() }) }
    AlertDialog(onDismissRequest = { if (!saving) onDismiss() }, title = { Text("تعديل السجل المحلي") }, text = {
        Column(Modifier.fillMaxWidth().heightIn(max = 380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(colors = listingFieldColors(), value = title, onValueChange = { title = it.take(120) }, label = { Text("العنوان") }, singleLine = true)
            OutlinedTextField(colors = listingFieldColors(), value = description, onValueChange = { description = it.take(3000) }, label = { Text("الوصف") }, minLines = 3)
            OutlinedTextField(colors = listingFieldColors(), value = category, onValueChange = { category = it.take(60) }, label = { Text("الفئة") }, singleLine = true)
            OutlinedTextField(colors = listingFieldColors(), value = location, onValueChange = { location = it.take(80) }, label = { Text("المدينة") }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val references = selectedImages ?: imageStore.imageReferences(row.imageUrl)
                references.take(3).forEach { reference ->
                    coil.compose.AsyncImage(model = imageStore.resolve(reference), contentDescription = "صورة السجل",
                        modifier = Modifier.size(64.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { picker.launch(androidx.activity.result.PickVisualMediaRequest(
                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !saving) { Text("تغيير الصور") }
                TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { selectedImages = arrayListOf() }, enabled = !saving &&
                    (selectedImages ?: imageStore.imageReferences(row.imageUrl)).isNotEmpty()) { Text("حذف الصور") }
            }
            Box { OutlinedButton(colors = com.example.designsystem.theme.ihsanOutlinedButtonColors(), onClick = { expanded = true }) { Text(statusLabel(status)) }
                DropdownMenu(expanded, { expanded = false }) { listOf("AVAILABLE", "PENDING", "COMPLETED").forEach { value -> DropdownMenuItem(text = { Text(statusLabel(value)) }, onClick = { status = value; expanded = false }) } } }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { onSave(row.copy(title = title, description = description, category = category, location = location, status = status), selectedImages) },
        enabled = !saving && listOf(title, description, category, location).all { it.isNotBlank() }) { Text(if (saving) "جارٍ الحفظ…" else "حفظ") } },
        dismissButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = onDismiss, enabled = !saving) { Text("إلغاء") } })
}

@Composable
private fun listingFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = IhsanTheme.colors.textBody, unfocusedTextColor = IhsanTheme.colors.textBody,
    focusedContainerColor = IhsanTheme.colors.fieldContainer, unfocusedContainerColor = IhsanTheme.colors.fieldContainer,
    cursorColor = IhsanTheme.colors.accentContent, focusedBorderColor = IhsanTheme.colors.fieldFocusedBorder,
    unfocusedBorderColor = IhsanTheme.colors.fieldBorder, focusedLabelColor = IhsanTheme.colors.textSecondary,
    unfocusedLabelColor = IhsanTheme.colors.textSecondary)
