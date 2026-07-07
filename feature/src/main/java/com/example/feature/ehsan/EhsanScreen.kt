package com.example.feature.ehsan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.presentation.EhsanViewModel
import com.example.feature.components.AuthBottomSheet
import com.example.feature.components.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EhsanScreen(
    onNavigateBack: () -> Unit = {},
    onAddEhsanClick: (String) -> Unit = {},
    onDonationClick: (Long) -> Unit = {},
    viewModel: EhsanViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    var showAuthSheet by remember { mutableStateOf(false) }
    var pendingActionType by remember { mutableStateOf("OFFER") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                EhsanTopBar(onNavigateBack)
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        pendingActionType = "OFFER"
                        if (currentUser == null) showAuthSheet = true else onAddEhsanClick("OFFER")
                    },
                    containerColor = Color(0xFF0D4D3D),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("إضافة إحسان", fontWeight = FontWeight.Bold) }
                )
            },
            containerColor = Color(0xFFF9F9F9)
        ) { padding ->
            if (showAuthSheet) {
                AuthBottomSheet(
                    onDismiss = { showAuthSheet = false },
                    onAuthSuccess = {
                        showAuthSheet = false
                        onAddEhsanClick(pendingActionType)
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header / Impact Card
                item {
                    ImpactSection(
                        donorCount = uiState.donorCount,
                        completedCount = uiState.completedCount
                    )
                }

                // Quick Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "أرغب بالتبرع",
                            subtitle = "عرض فائض لديك",
                            icon = Icons.Default.VolunteerActivism,
                            containerColor = Color(0xFF0D4D3D),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                pendingActionType = "OFFER"
                                if (currentUser == null) showAuthSheet = true else onAddEhsanClick("OFFER")
                            }
                        )
                        QuickActionCard(
                            title = "أحتاج مساعدة",
                            subtitle = "اطلب ما ينقصك",
                            icon = Icons.Default.Handshake,
                            containerColor = Color.White,
                            contentColor = Color(0xFF0D4D3D),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                pendingActionType = "REQUEST"
                                if (currentUser == null) showAuthSheet = true else onAddEhsanClick("REQUEST")
                            }
                        )
                    }
                }

                // Search and Filters
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange
                        )
                        
                        LocationAndCategoryFilters(
                            selectedLocation = uiState.selectedLocation,
                            onLocationSelected = viewModel::onLocationChange,
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = viewModel::onCategoryChange
                        )

                        TypeSelectionTabs(
                            selectedType = uiState.selectedType,
                            onTypeSelected = viewModel::onTypeChange
                        )
                    }
                }

                item {
                    Text(
                        text = "فرص إحسان المتاحة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF0D4D3D))
                        }
                    }
                } else if (uiState.filteredDonations.isEmpty()) {
                    item {
                        EhsanEmptyState(
                            query = uiState.searchQuery,
                            modifier = Modifier.fillParentMaxHeight(0.5f)
                        )
                    }
                } else {
                    items(uiState.filteredDonations, key = { it.id }) { donation ->
                        DonationListItem(
                            donation = donation,
                            onClick = { onDonationClick(donation.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EhsanTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text("إحسان", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Notifications, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun ImpactSection(donorCount: Int, completedCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0D4D3D)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("مجتمع إحسان", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text("يداً بيد لنشر الخير", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImpactStatItem(Modifier.weight(1f), "متبرع", donorCount.toString())
                ImpactStatItem(Modifier.weight(1f), "حالة مكتملة", completedCount.toString())
                ImpactStatItem(Modifier.weight(1f), "مدينة", "١٢")
            }
        }
    }
}

@Composable
private fun ImpactStatItem(modifier: Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = contentColor.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        placeholder = { Text("ابحث عن غرض، مدينة، أو محتاج...", fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun LocationAndCategoryFilters(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val locations = listOf("الكل", "حلب", "دمشق", "حمص", "حماة")
    val categories = listOf("الكل", "طعام", "ملابس", "أثاث", "أخرى")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(locations) { loc ->
                FilterChip(
                    selected = selectedLocation == loc,
                    onClick = { onLocationSelected(loc) },
                    label = { Text(loc) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0D4D3D),
                        selectedLabelColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategorySelected(cat) },
                    label = { Text(cat) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6B9080),
                        selectedLabelColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun TypeSelectionTabs(selectedType: String, onTypeSelected: (String) -> Unit) {
    TabRow(
        selectedTabIndex = when(selectedType) { "OFFER" -> 1; "REQUEST" -> 2; else -> 0 },
        containerColor = Color.Transparent,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[when(selectedType) { "OFFER" -> 1; "REQUEST" -> 2; else -> 0 }]),
                color = Color(0xFF0D4D3D)
            )
        }
    ) {
        Tab(selected = selectedType == "ALL", onClick = { onTypeSelected("ALL") }) {
            Text("الكل", modifier = Modifier.padding(12.dp), fontSize = 14.sp)
        }
        Tab(selected = selectedType == "OFFER", onClick = { onTypeSelected("OFFER") }) {
            Text("عروض تبرع", modifier = Modifier.padding(12.dp), fontSize = 14.sp)
        }
        Tab(selected = selectedType == "REQUEST", onClick = { onTypeSelected("REQUEST") }) {
            Text("طلبات مساعدة", modifier = Modifier.padding(12.dp), fontSize = 14.sp)
        }
    }
}

@Composable
private fun DonationListItem(donation: Donation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image or Icon
            Surface(
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)),
                color = Color(0xFFF5F5F5)
            ) {
                if (donation.imageUrl != null) {
                    AsyncImage(
                        model = donation.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = when(donation.category) {
                            "طعام" -> Icons.Default.Restaurant
                            "ملابس" -> Icons.Default.Checkroom
                            "أثاث" -> Icons.Default.Weekend
                            else -> Icons.Default.Category
                        },
                        contentDescription = null,
                        modifier = Modifier.padding(24.dp),
                        tint = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (donation.type == "OFFER") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (donation.type == "OFFER") "تبرع" else "طلب",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = if (donation.type == "OFFER") Color(0xFF2E7D32) else Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = donation.location,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = donation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = donation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF0D4D3D))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(donation.donorName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.LightGray)
                }
            }
        }
    }
}

@Composable
private fun EhsanEmptyState(query: String, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = Color(0xFFF5F5F5)
        ) {
            Icon(
                imageVector = if (query.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.VolunteerActivism,
                contentDescription = null,
                modifier = Modifier.padding(24.dp),
                tint = Color.LightGray
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isNotEmpty()) "لا توجد نتائج لـ \"$query\"" else "لا توجد فرص إحسان متاحة حالياً",
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}
