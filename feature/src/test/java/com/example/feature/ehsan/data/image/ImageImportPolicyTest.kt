package com.example.feature.ehsan.data.image
import java.io.*
import org.junit.Assert.*
import org.junit.Test
class ImageImportPolicyTest {
 @Test fun `copy rejects unknown provider size over budget without writing excess`() {
  val output = ByteArrayOutputStream()
  try { ImageImportPolicy.copy(ByteArrayInputStream(ByteArray(33)), output, 32); fail() } catch (_: IOException) { }
  assertTrue(output.size() <= 32)
 }
 @Test fun `exact budget succeeds and empty file fails`() {
  val out = ByteArrayOutputStream(); assertEquals(32L, ImageImportPolicy.copy(ByteArrayInputStream(ByteArray(32)),out,32))
  try { ImageImportPolicy.copy(ByteArrayInputStream(byteArrayOf()), out); fail() } catch (_: IOException) { }
 }
 @Test fun `mime requires known raster image`() {
  assertEquals(".jpg", ImageImportPolicy.extension("image/jpeg")); assertEquals(".png",ImageImportPolicy.extension("image/png"))
  listOf(null,"image/svg+xml","text/html","application/octet-stream").forEach { assertNull(ImageImportPolicy.extension(it)) }
 }
 @Test fun `pixel budget uses long arithmetic and rejects malformed dimensions`() {
  assertTrue(ImageImportPolicy.validDimensions(8000,5000)); assertFalse(ImageImportPolicy.validDimensions(8001,5000))
  assertFalse(ImageImportPolicy.validDimensions(Int.MAX_VALUE,Int.MAX_VALUE)); assertFalse(ImageImportPolicy.validDimensions(-1,1))
 }
}
