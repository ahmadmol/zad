package com.example.feature.azkar.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface DateProvider {
    fun getCurrentDate(): String
}

class DateProviderImpl : DateProvider {
    override fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}