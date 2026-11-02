package com.example.feature.ehsan
import com.example.feature.ehsan.domain.model.LocalListingValidation
import org.junit.Assert.*
import org.junit.Test
class LocalListingValidationTest {
 @Test fun `new and edited records share limits and explicit types`() {
  assertNull(LocalListingValidation.error("عنوان","وصف","كتب","حلب","OFFER"))
  assertNotNull(LocalListingValidation.error("عنوان","وصف","كتب","حلب","DEMO"))
  assertNotNull(LocalListingValidation.error("x".repeat(121),"وصف","كتب","حلب","REQUEST"))
  assertNotNull(LocalListingValidation.error("عنوان","وصف","كتب"," ","REQUEST"))
  assertNotNull(LocalListingValidation.error("عنوان","وصف","كتب","حلب","OFFER","fake"))
 }
}
