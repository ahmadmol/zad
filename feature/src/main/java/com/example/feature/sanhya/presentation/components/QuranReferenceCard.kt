package com.example.feature.sanhya.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.feature.learning.presentation.components.RelatedContentCard
import com.example.feature.sanhya.domain.model.QuranReference

@Composable
fun QuranReferenceCard(reference: QuranReference, onViewVersesClick: () -> Unit, modifier: Modifier = Modifier) {
    RelatedContentCard("سورة ${reference.surahName}", reference.ayahRangeLabel + " — عرض الآيات", onViewVersesClick, modifier)
}
