package com.example.feature.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.feature.core.util.HijriDateProvider
import java.time.temporal.ChronoField

/** Preserve at least 48dp per interactive day; large fonts can scroll the week without clipping. */
@Composable
fun HijriMonthGrid(days: List<com.example.feature.core.util.HijriCalendarDay>, today: java.time.LocalDate,
    selectedDay: Long?, annotations: Map<Long, String> = emptyMap(),
    isDayEnabled: (java.time.LocalDate) -> Boolean = { true }, onSelect: (Long) -> Unit) {
    val fontScale=LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val availableWidth=maxWidth
        val gridWidth=maxOf(availableWidth,336.dp*fontScale)
        Column {
            if(gridWidth>availableWidth) Text("اسحب الأسبوع أفقيًا لعرض بقية الأيام",style=MaterialTheme.typography.labelSmall)
            Column(Modifier.horizontalScroll(rememberScrollState())) {
                Column(Modifier.width(gridWidth)) {
                    Row(Modifier.fillMaxWidth()) {
                        listOf("السبت","الأحد","الإثنين","الثلاثاء","الأربعاء","الخميس","الجمعة").forEach {
                            Text(it,modifier=Modifier.weight(1f),style=MaterialTheme.typography.labelSmall,textAlign=TextAlign.Center)
                        }
                    }
                    val blanks=(days.first().civil.dayOfWeek.value+1)%7
                    val cells=List<com.example.feature.core.util.HijriCalendarDay?>(blanks){null}+days
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                Box(Modifier.weight(1f)) {
                                    if(day!=null) Surface(onClick={onSelect(day.civil.toEpochDay())}, enabled=isDayEnabled(day.civil),
                                        modifier=Modifier.fillMaxWidth().heightIn(min=64.dp).semantics(mergeDescendants=true) {
                                            describeCalendarDay(day,today,selectedDay,annotations[day.civil.toEpochDay()])
                                        },
                                        color=if(day.civil.toEpochDay()==selectedDay) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                                        shape=MaterialTheme.shapes.small) {
                                        Column(Modifier.padding(2.dp),horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally) {
                                            Text("${day.hijri.get(ChronoField.DAY_OF_MONTH)}",style=MaterialTheme.typography.bodyMedium)
                                            Text("${day.civil.dayOfMonth}/${day.civil.monthValue}",style=MaterialTheme.typography.labelSmall)
                                            annotations[day.civil.toEpochDay()]?.let { Text(it,style=MaterialTheme.typography.labelSmall) }
                                            if(day.civil==today) Text("اليوم",style=MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                            repeat(7-week.size){Spacer(Modifier.weight(1f))}
                        }
                    }
                }
            }
        }
    }
}

/** Semantics receivers expose write-only properties; compose the full label before setting it. */
internal fun androidx.compose.ui.semantics.SemanticsPropertyReceiver.describeCalendarDay(
    day: com.example.feature.core.util.HijriCalendarDay, today: java.time.LocalDate, selectedDay: Long?, annotation: String?) {
    contentDescription="${HijriDateProvider.format(day.hijri)}، ${day.civil} ميلادي${if(day.civil==today) "، اليوم" else ""}${annotation?.let { "، $it" }.orEmpty()}"
    selected=day.civil.toEpochDay()==selectedDay
}
