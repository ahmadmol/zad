package com.example.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.feature.R
import com.example.feature.quran.domain.model.QuranRepeatConfig
import com.example.feature.quran.domain.model.QuranRepeatMode
import kotlin.math.roundToInt

/**
 * Phase 4 — Repeat Range control.
 *
 * Lets the reader repeat the current ayah or a closed range a fixed number of times
 * (or until stopped). The sheet only produces a normalized [QuranRepeatConfig]; all
 * playback sequencing lives in [com.example.feature.quran.domain.model.QuranRepeatPlanner].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranRepeatSheet(
    current: QuranRepeatConfig,
    ayahCount: Int,
    currentAyah: Int?,
    onApply: (QuranRepeatConfig) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxAyah = ayahCount.coerceAtLeast(1)

    var mode by remember {
        mutableStateOf(
            if (current.isActive) current.mode else QuranRepeatMode.SINGLE_AYAH
        )
    }
    var start by remember {
        mutableFloatStateOf(
            (if (current.isActive) current.startAyah else currentAyah ?: 1)
                .coerceIn(1, maxAyah).toFloat()
        )
    }
    var end by remember {
        mutableFloatStateOf(
            (if (current.isActive) current.endAyah else currentAyah ?: 1)
                .coerceIn(1, maxAyah).toFloat()
        )
    }
    // 0 on the slider means "unlimited".
    var count by remember { mutableFloatStateOf(current.repeatCount.coerceIn(0, 20).toFloat()) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                text = stringResource(R.string.quran_repeat_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == QuranRepeatMode.SINGLE_AYAH,
                    onClick = { mode = QuranRepeatMode.SINGLE_AYAH },
                    label = { Text(stringResource(R.string.quran_repeat_single_ayah)) },
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = mode == QuranRepeatMode.RANGE,
                    onClick = { mode = QuranRepeatMode.RANGE },
                    label = { Text(stringResource(R.string.quran_repeat_range)) },
                    colors = FilterChipDefaults.filterChipColors()
                )
            }

            Spacer(Modifier.height(20.dp))

            if (mode == QuranRepeatMode.SINGLE_AYAH) {
                LabelledAyahSlider(
                    label = stringResource(R.string.quran_repeat_ayah),
                    value = start,
                    maxAyah = maxAyah,
                    onValueChange = { start = it; end = it }
                )
            } else {
                LabelledAyahSlider(
                    label = stringResource(R.string.quran_repeat_from),
                    value = start,
                    maxAyah = maxAyah,
                    onValueChange = { start = it }
                )
                Spacer(Modifier.height(12.dp))
                LabelledAyahSlider(
                    label = stringResource(R.string.quran_repeat_to),
                    value = end,
                    maxAyah = maxAyah,
                    onValueChange = { end = it }
                )
            }

            Spacer(Modifier.height(20.dp))

            val countLabel =
                if (count.roundToInt() == QuranRepeatConfig.UNLIMITED) {
                    stringResource(R.string.quran_repeat_unlimited)
                } else {
                    stringResource(R.string.quran_repeat_times, count.roundToInt())
                }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.quran_repeat_count),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(text = countLabel, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = count,
                onValueChange = { count = it },
                valueRange = 0f..20f,
                steps = 19,
                modifier = Modifier.semantics { contentDescription = countLabel }
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (current.isActive) {
                    TextButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.quran_repeat_stop))
                    }
                }
                Button(
                    onClick = {
                        val config = when (mode) {
                            QuranRepeatMode.RANGE -> QuranRepeatConfig.range(
                                start = start.roundToInt(),
                                end = end.roundToInt(),
                                ayahCount = maxAyah,
                                repeatCount = count.roundToInt()
                            )

                            else -> QuranRepeatConfig.singleAyah(
                                ayah = start.roundToInt(),
                                ayahCount = maxAyah,
                                repeatCount = count.roundToInt()
                            )
                        }
                        onApply(config)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.quran_repeat_apply))
                }
            }
        }
    }
}

@Composable
private fun LabelledAyahSlider(
    label: String,
    value: Float,
    maxAyah: Int,
    onValueChange: (Float) -> Unit
) {
    val description = label + " " + value.roundToInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value.roundToInt().toString(), fontWeight = FontWeight.Bold)
    }
    Slider(
        value = value.coerceIn(1f, maxAyah.toFloat()),
        onValueChange = onValueChange,
        valueRange = 1f..maxAyah.toFloat(),
        modifier = Modifier.semantics { contentDescription = description }
    )
}
