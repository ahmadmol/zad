package com.example.feature.ehsan.domain.model

/** Local form limits, shared by creation and editing. No publication or identity verification. */
object LocalListingValidation {
    fun error(title: String, description: String, category: String, location: String,
              type: String, status: String = "AVAILABLE"): String? = when {
        title.isBlank() || title.length > 120 -> "أدخل عنوانًا من 1 إلى 120 حرفًا"
        description.isBlank() || description.length > 3000 -> "أدخل وصفًا من 1 إلى 3000 حرف"
        category.isBlank() || category.length > 60 -> "أدخل فئة من 1 إلى 60 حرفًا"
        location.isBlank() || location.length > 80 -> "أدخل مدينة من 1 إلى 80 حرفًا"
        type !in setOf("OFFER", "REQUEST") -> "نوع السجل غير مدعوم"
        status !in setOf("AVAILABLE", "PENDING", "COMPLETED") -> "حالة السجل غير مدعومة"
        else -> null
    }
    fun requireValid(row: Donation) {
        require(error(row.title, row.description, row.category, row.location, row.type, row.status) == null) {
            error(row.title, row.description, row.category, row.location, row.type, row.status).orEmpty()
        }
    }
}
