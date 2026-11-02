package com.example.feature.batch05
import com.example.feature.core.observability.ObservabilitySanitizer
import org.junit.Assert.*
import org.junit.Test
class Batch05LoggingTest {
 @Test fun `logs redact credentials URIs phone and email`() {
  val value=ObservabilitySanitizer.sanitize("token=private-value-817 password=hidden https://example.test/private?key=abc content://provider/person a@example.com +963912345678")
  listOf("private-value-817","hidden","provider/person","private?key","a@example.com","963912345678").forEach { assertFalse(value,value.contains(it)) }
 }
}
