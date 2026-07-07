package com.example.feature.asma.domain.util

import com.example.feature.asma.domain.model.AllahName
import java.util.Calendar
import java.util.Date

object AsmaTodayResolver {
    fun selectDailyName(names: List<AllahName>, date: Date = Date()): AllahName? {
        if (names.isEmpty()) return null
        val calendar = Calendar.getInstance().apply { time = date }
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val index = ((year + dayOfYear) % names.size + names.size) % names.size
        return names[index]
    }
}
