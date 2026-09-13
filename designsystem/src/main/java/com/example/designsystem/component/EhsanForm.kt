package com.example.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/* Re-export commonly-used spacer values so screens can keep
 * their gap rhythm consistent without re-declaring them. */
object EhsanFormSpacing {
    val SectionGap: Dp = 24.dp
    val CardGap: Dp = 16.dp
    val FieldGap: Dp = 16.dp
    val InlineGap: Dp = 8.dp
}
