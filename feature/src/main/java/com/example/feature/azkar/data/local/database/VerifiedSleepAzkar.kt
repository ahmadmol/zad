package com.example.feature.azkar.data.local.database

import android.content.Context
import com.example.feature.azkar.data.local.entity.ZikrEntity
import kotlinx.serialization.json.*

object VerifiedSleepAzkar {
    fun load(context: Context): List<ZikrEntity> {
        val raw = context.assets.open("verified_sleep_azkar.json").bufferedReader().use { it.readText() }
        return Json.parseToJsonElement(raw).jsonArray.map { item ->
            val row = item.jsonObject
            ZikrEntity(title = row.getValue("title").jsonPrimitive.content,
                text = row.getValue("text").jsonPrimitive.content,
                category = row.getValue("category").jsonPrimitive.content,
                targetCount = row.getValue("targetCount").jsonPrimitive.int,
                source = row.getValue("source").jsonPrimitive.content)
        }
    }
}
