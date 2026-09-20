package com.example.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

/* ============================================================
 * EhsanForm.kt
 * ------------------------------------------------------------
 * A small, opinionated kit of M3 form primitives shared by the
 * "Add Donation" and "Request Help" screens.
 *
 * Goals:
 *   • One calm visual rhythm across both flows.
 *   • Strong but soft hierarchy — section → label → field.
 *   • Liquid Glass is reserved for the section cards (cards
 *     that benefit from depth). Fields use a flat tonal fill
 *     so the text inside stays perfectly legible.
 *   • RTL is honored because all paddings use Modifier
 *     .padding(horizontal = ...) — no hard-coded Left/Right.
 *   • The designsystem module deliberately does NOT depend on
 *     Coil — image rendering is delegated to the consumer via
 *     a content slot in [EhsanImagePicker].
 * ============================================================ */

/* --- Tokens (kept private; consumers go through composables) --- */
private val FormCornerLarge: Dp = 20.dp
private val FormCornerMedium: Dp = 16.dp
private val FormCornerSmall: Dp = 12.dp
private val FormFieldMinHeight: Dp = 56.dp
private val FormCardPadding: Dp = 20.dp

/**
 * Section card — the calm "container" that groups related fields.
 *
 * Visual recipe:
 *   • soft white/elevated surface
 *   • 1px hairline border
 *   • 20dp padding inside, 20dp corner radius
 *   • optional `LiquidGlass(Elevated)` for a hint of sheen on
 *     the topmost card of the form
 */
@Composable
fun EhsanFormCard(
    modifier: Modifier = Modifier,
    useLiquidGlass: Boolean = false,
    contentPadding: Dp = FormCardPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = IhsanTheme.colors
    val shape = RoundedCornerShape(FormCornerLarge)

    val baseModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(colors.surfaceElevated)
        .border(
            width = 1.dp,
            color = colors.borderSubtle,
            shape = shape
        )

    val finalModifier = if (useLiquidGlass) {
        baseModifier.liquidGlass(
            style = LiquidGlassStyle.Elevated,
            shape = shape,
            cornerRadius = 0.dp // already clipped
        )
    } else baseModifier

    Column(modifier = finalModifier.padding(contentPadding), content = content)
}

/**
 * Section header — title + optional supporting copy.
 * Stays outside the card so the title reads as a paragraph of
 * the page, not as part of the input.
 */
@Composable
fun EhsanSectionHeader(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = IhsanTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
        if (!description.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = IhsanTheme.colors.textSecondary
            )
        }
    }
}

/**
 * Label rendered above a field.
 *
 * We keep a small uppercase-feeling style: 12sp, medium weight,
 * 0.5 tracking — it gives the form a calm "form-like" rhythm
 * without screaming "label" in all caps.
 */
@Composable
fun EhsanFieldLabel(
    text: String,
    required: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = LocalTextStyle.current.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = IhsanTheme.colors.textSecondary
        )
        if (required) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = "•",
                color = IhsanTheme.colors.brand,
                style = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Standard text input — the workhorse of both forms.
 *
 *  • Outlined M3 field
 *  • Rounded 16dp
 *  • Subtle border that becomes the brand green on focus
 *  • Optional helper text and error state
 *  • Optional leading icon
 */
@Composable
fun EhsanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
    readOnly: Boolean = false,
    enabled: Boolean = true
) {
    val colors = IhsanTheme.colors
    val isError = errorText != null

    Column(modifier = modifier.fillMaxWidth()) {
        EhsanFieldLabel(text = label)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = FormFieldMinHeight),
            placeholder = {
                if (!placeholder.isNullOrBlank()) {
                    Text(
                        text = placeholder,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            shape = RoundedCornerShape(FormCornerMedium),
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            readOnly = readOnly,
            enabled = enabled,
            isError = isError,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else colors.fieldFocusedBorder,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else colors.fieldBorder,
                disabledBorderColor = colors.borderSubtle,
                focusedContainerColor = colors.fieldContainer,
                unfocusedContainerColor = colors.fieldContainer,
                disabledContainerColor = colors.surfaceMuted,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                disabledTextColor = colors.textDisabled
            )
        )

        if (isError) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = errorText ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        } else if (!helperText.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = helperText,
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Read-only "selector" field — used for City picker.
 * Behaves like a TextField but is tappable and shows a caret
 * instead of a cursor.
 */
@Composable
fun EhsanSelectorField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    errorText: String? = null
) {
    val colors = IhsanTheme.colors
    val display = value.ifBlank { placeholder.orEmpty() }
    val isError = errorText != null

    Column(modifier = modifier.fillMaxWidth()) {
        EhsanFieldLabel(text = label)

        Spacer(Modifier.height(8.dp))

        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(FormCornerMedium),
            color = colors.fieldContainer,
            border = BorderStroke(
                width = 1.dp,
                color = if (isError) MaterialTheme.colorScheme.error else colors.fieldBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = FormFieldMinHeight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Text(
                    text = display,
                    modifier = Modifier.weight(1f),
                    color = if (value.isBlank()) colors.textSecondary else colors.textPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isError) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = errorText ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Segmented toggle — pick between two related intents
 * (e.g. "تبرع" vs "مساعدة"). M3 FilterChip-but-toned-down
 * so it does not feel like a row of pills.
 */
@Composable
fun EhsanSegmentedToggle(
    options: List<SegmentedOption>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors
    val shape = RoundedCornerShape(FormCornerSmall)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceMuted)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.key == selectedKey
            Surface(
                onClick = { onSelect(option.key) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) colors.surfaceElevated else Color.Transparent,
                border = if (isSelected) BorderStroke(1.dp, colors.borderSubtle) else null,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (option.icon != null) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (isSelected) colors.brand else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = option.label,
                        color = if (isSelected) colors.textPrimary else colors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

data class SegmentedOption(
    val key: String,
    val label: String,
    val icon: ImageVector? = null
)

/**
 * Chip row used to choose a single category. Wraps content so
 * long category lists do not horizontally scroll-cut labels.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EhsanChoiceChipRow(
    options: List<SegmentedOption>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.key == selectedKey
            Surface(
                onClick = { onSelect(option.key) },
                shape = RoundedCornerShape(999.dp),
                color = if (isSelected) colors.charityOfferContainer else colors.surfaceMuted,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) colors.brand.copy(alpha = 0.2f) else colors.borderSubtle
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (option.icon != null) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (isSelected) colors.brand else colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = option.label,
                        color = if (isSelected) colors.brand else colors.textPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Image picker — calm, premium, with two states.
 *
 * Empty: tonal rounded surface, a small icon tile, and copy
 * that explains why an image helps ("اختياري").
 *
 * Filled: the consumer's [imageContent] (typically a Coil
 * AsyncImage) fills the card, with a small floating "remove"
 * button so the user is not trapped with the wrong photo.
 *
 * Note: This composable deliberately does NOT depend on Coil
 * — the host screen supplies the actual image rendering so
 * the designsystem module stays light.
 */
@Composable
fun EhsanImagePicker(
    imageUri: String?,
    onPick: () -> Unit,
    onClear: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    emptyTitle: String,
    emptyHint: String? = null,
    imageContent: @Composable () -> Unit = {}
) {
    val colors = IhsanTheme.colors
    val shape = RoundedCornerShape(FormCornerLarge)

    Column(modifier = modifier.fillMaxWidth()) {
        EhsanFieldLabel(text = label)
        Spacer(Modifier.height(8.dp))

        if (imageUri == null) {
            // Empty state — calm, generous
            Surface(
                onClick = onPick,
                shape = shape,
                color = colors.surfaceMuted,
                border = BorderStroke(1.dp, colors.borderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = colors.brand,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = emptyTitle,
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    if (!emptyHint.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = emptyHint,
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Filled state — image fills the card, with a remove
            // affordance so the user can change their mind.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(shape)
                    .background(colors.surfaceMuted)
                    .border(1.dp, colors.borderSubtle, shape)
            ) {
                imageContent()

                Surface(
                    onClick = onClear,
                    shape = RoundedCornerShape(999.dp),
                    color = colors.surfaceElevated.copy(alpha = 0.95f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إزالة الصورة",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Primary action — pill button that spans the form width.
 * Uses the brand teal as container and a clear "label + icon"
 * affordance.
 */
@Composable
fun EhsanPrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    val colors = IhsanTheme.colors
    Surface(
        onClick = { if (enabled && !isLoading) onClick() },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(28.dp),
        color = if (enabled) colors.brand else colors.surfaceMuted,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = colors.onBrand,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = text,
                    color = if (enabled) colors.onBrand else colors.textDisabled,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (icon != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) colors.onBrand else colors.textDisabled,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Quiet "contact info" card — used when the form needs to
 * remind the user what name/phone will be shared with the
 * post (read-only).
 */
@Composable
fun EhsanContactCard(
    name: String,
    phone: String,
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors

    Surface(
        shape = RoundedCornerShape(FormCornerMedium),
        color = colors.surfaceMuted,
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "سيتم استخدام هذه المعلومات للتواصل معك",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(10.dp))
            ReadOnlyRow(label = "الاسم", value = name)
            Spacer(Modifier.height(6.dp))
            ReadOnlyRow(label = "الجوال", value = phone)
        }
    }
}

@Composable
private fun ReadOnlyRow(label: String, value: String) {
    val colors = IhsanTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value.ifBlank { "—" },
            color = colors.textPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Unified Hero Banner Header for Ehsan Create flows (Offer / Request).
 * Features cyan sky-blue gradient, mosque silhouette artwork, integrated status bar,
 * top navigation buttons, center logo, city/date badge, title, subtitle, and curved wave transition.
 */
@Composable
fun EhsanHeroHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    cityName: String? = "حلب",
    islamicDate: String? = "12 ربيع الأول 1448",
    logoPainter: Painter? = null,
    onSearchClick: (() -> Unit)? = null
) {
    val isDark = IhsanTheme.isDark
    val colors = IhsanTheme.colors
    val heroContent = colors.onBrand

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier.matchParentSize()
        )

        if (isDark) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Row (RTL: First child = Right, Third child = Left)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Right: Location & Islamic Date Info (First child in RTL = Visual Right)
                Column(horizontalAlignment = Alignment.Start) {
                    if (!cityName.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = cityName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = heroContent
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = heroContent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    if (!islamicDate.isNullOrBlank()) {
                        Text(
                            text = islamicDate,
                            fontSize = 10.sp,
                            color = heroContent.copy(alpha = 0.85f)
                        )
                    }
                }

                // Center: Logo / Title badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (logoPainter != null) {
                        Image(
                            painter = logoPainter,
                            contentDescription = "إحسان",
                            modifier = Modifier.height(34.dp),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(heroContent)
                        )
                    } else {
                        Text(
                            text = "إحسان",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = heroContent
                        )
                    }
                    Text(
                        text = "خير دائم",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = heroContent.copy(alpha = 0.9f)
                    )
                }

                // Left: Back button & optional Search (Third child in RTL = Visual Left)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = heroContent
                        )
                    }
                    if (onSearchClick != null) {
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = heroContent
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Main Hero Title
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = heroContent,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            // Subtitle
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = heroContent.copy(alpha = 0.88f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Step Indicator — 3-stage progress stepper matching reference UI.
 */
data class StepItem(
    val stepNumber: Int,
    val title: String
)

@Composable
fun EhsanStepIndicator(
    steps: List<StepItem>,
    currentStep: Int,
    onStepClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors

    Surface(
        shape = RoundedCornerShape(FormCornerLarge),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                val isActive = currentStep == step.stepNumber
                val isCompleted = currentStep > step.stepNumber
                val isAccessible = step.stepNumber <= currentStep

                // Step Circle + Title Item
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(enabled = isAccessible) {
                        onStepClick(step.stepNumber)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive || isCompleted -> colors.brand
                                    else -> colors.surfaceMuted
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    isActive || isCompleted -> colors.brand
                                    else -> colors.borderSubtle
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.onBrand,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "${step.stepNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) colors.onBrand else colors.textSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive || isCompleted) colors.brand else colors.textSecondary
                    )
                }

                // Connecting Line between steps
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 8.dp)
                            .background(
                                if (currentStep > step.stepNumber) colors.brand else colors.borderSubtle
                            )
                    )
                }
            }
        }
    }
}

/**
 * Category Cards Grid/Row matching reference UI (e.g., ملابس، أثاث، مواد غذائية، كتب).
 * Displays selected card in dark teal with top-corner checkmark badge.
 */
data class CategoryCardOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun EhsanCategoryGrid(
    categories: List<CategoryCardOption>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category.key == selectedKey

            Surface(
                onClick = { onSelect(category.key) },
                shape = RoundedCornerShape(FormCornerMedium),
                color = if (isSelected) colors.brand else colors.fieldContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) colors.brand else colors.fieldBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 78.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Selected checkmark badge in corner
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(colors.onBrand.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.onBrand,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (isSelected) colors.onBrand else colors.brand,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.onBrand else colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Contact Method Selector matching reference UI (واتساب / اتصال هاتفي).
 */
@Composable
fun EhsanContactMethodSelector(
    selectedMethod: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors

    val options = listOf(
        Triple("WHATSAPP", "واتساب", Icons.AutoMirrored.Filled.Comment),
        Triple("PHONE", "اتصال هاتفي", Icons.Default.Phone)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { (key, label, icon) ->
            val isSelected = selectedMethod == key

            Surface(
                onClick = { onSelect(key) },
                shape = RoundedCornerShape(FormCornerMedium),
                color = if (isSelected) colors.charityOfferContainer else colors.fieldContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) colors.brand else colors.fieldBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) colors.brand else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colors.brand else colors.textPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    // Radio Circle
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                if (isSelected) colors.brand else colors.textSecondary.copy(alpha = 0.5f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors.brand)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Review Summary Card shown in Step 3 before publishing/submitting.
 */
@Composable
fun EhsanReviewSummaryCard(
    title: String,
    category: String,
    city: String,
    description: String,
    contactPhone: String,
    modifier: Modifier = Modifier
) {
    EhsanFormCard(modifier = modifier) {
        EhsanSectionHeader(
            title = "مراجعة التفاصيل",
            description = "تأكد من صحة البيانات قبل النشر"
        )
        Spacer(Modifier.height(14.dp))
        ReadOnlyRow(label = "العنوان", value = title)
        Spacer(Modifier.height(8.dp))
        ReadOnlyRow(label = "التصنيف", value = category)
        Spacer(Modifier.height(8.dp))
        ReadOnlyRow(label = "المدينة", value = city)
        Spacer(Modifier.height(8.dp))
        ReadOnlyRow(label = "رقم التواصل", value = contactPhone)
        Spacer(Modifier.height(8.dp))
        ReadOnlyRow(label = "الوصف", value = description)
    }
}

/* Re-export commonly-used spacer values so screens can keep
 * their gap rhythm consistent without re-declaring them. */
object EhsanFormSpacing {
    val SectionGap: Dp = 24.dp
    val CardGap: Dp = 16.dp
    val FieldGap: Dp = 16.dp
    val InlineGap: Dp = 8.dp
}

