package com.example.feature.core.observability

/**
 * Privacy-safe operational logger. Implementations must not log PII,
 * phone numbers, addresses, precise coordinates, Quran text, or charity descriptions.
 */
interface AppLogger {
    fun debug(tag: String, message: String)
    fun warning(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Operational event reporter. Events must remain free of personal content.
 */
interface AppEventReporter {
    fun report(event: AppOperationalEvent)
}

data class AppOperationalEvent(
    val name: String,
    val category: AppOperationalCategory,
    val attributes: Map<String, String> = emptyMap()
)

enum class AppOperationalCategory {
    PrayerScheduling,
    DatabaseMigration,
    QuranAudio,
    EhsanData,
    ReleaseGate
}

/** Debug / development Logcat sink. */
class LogcatAppLogger : AppLogger {
    override fun debug(tag: String, message: String) {
        android.util.Log.d(tag, sanitize(message))
    }

    override fun warning(tag: String, message: String, throwable: Throwable?) {
        android.util.Log.w(tag, sanitize(message), throwable)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        android.util.Log.e(tag, sanitize(message), throwable)
    }

    private fun sanitize(message: String): String =
        ObservabilitySanitizer.sanitize(message)
}

/** Release default until a vendor is approved. */
object NoOpAppLogger : AppLogger {
    override fun debug(tag: String, message: String) = Unit
    override fun warning(tag: String, message: String, throwable: Throwable?) = Unit
    override fun error(tag: String, message: String, throwable: Throwable?) = Unit
}

object NoOpAppEventReporter : AppEventReporter {
    override fun report(event: AppOperationalEvent) = Unit
}

object ObservabilitySanitizer {
    private val phonePattern = Regex("""\+?\d[\d\s\-()]{7,}\d""")
    private val emailPattern = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val coordPattern = Regex("""-?\d{1,3}\.\d{3,}""")

    fun sanitize(message: String): String =
        message
            .replace(phonePattern, "[redacted-phone]")
            .replace(emailPattern, "[redacted-email]")
            .replace(coordPattern, "[redacted-coord]")

    fun containsForbiddenPii(message: String): Boolean =
        phonePattern.containsMatchIn(message) ||
            emailPattern.containsMatchIn(message) ||
            coordPattern.containsMatchIn(message)

    fun assertSafeAttributes(attributes: Map<String, String>) {
        attributes.forEach { (key, value) ->
            require(!key.equals("phone", ignoreCase = true)) { "phone attribute forbidden" }
            require(!key.equals("address", ignoreCase = true)) { "address attribute forbidden" }
            require(!key.equals("latitude", ignoreCase = true)) { "latitude attribute forbidden" }
            require(!key.equals("longitude", ignoreCase = true)) { "longitude attribute forbidden" }
            require(!containsForbiddenPii(value)) { "PII detected in attribute $key" }
        }
    }
}
