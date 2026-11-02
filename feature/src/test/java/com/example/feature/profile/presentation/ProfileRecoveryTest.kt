package com.example.feature.profile.presentation
import androidx.lifecycle.SavedStateHandle
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.repository.UserRepository
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.core.preferences.UserPreferences
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.io.IOException
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRecoveryTest {
 private val dispatcher=StandardTestDispatcher()
 @Before fun before() { Dispatchers.setMain(dispatcher) }
 @After fun after() { Dispatchers.resetMain() }
 @Test fun `an immediate initial read retains owner identity and saves successfully`() = runTest {
  Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
  val users=mockk<UserRepository>(relaxUnitFun=true);val prefs=mockk<UserPreferences>(relaxUnitFun=true)
  every {users.getUser()} returns flowOf(UserEntity(1,"Fixture","Name","","fixture",""))
  every {prefs.userAvatarUri} returns flowOf(null);every {prefs.userBio} returns flowOf("")
  val vm=EditProfileViewModel(users,prefs,mockk(),SavedStateHandle());vm.saveChanges();runCurrent()
  coVerify(exactly=1) {users.updateProfile("Fixture","Name","fixture","")}
  assertNotNull(vm.uiState.value.successMessage)
 }
 @Test fun `failed auxiliary read blocks saving and avatar import until successful retry`()=runTest {
  val users=mockk<UserRepository>(relaxUnitFun=true);val prefs=mockk<UserPreferences>();val images=mockk<EhsanImageStore>()
  every {users.getUser()} returns flowOf(UserEntity(1,"Fixture","Name","","fixture",""))
  every {prefs.userAvatarUri} returns flowOf(null);every {prefs.userBio} returns flow {throw IOException()}
  val vm=EditProfileViewModel(users,prefs,images,SavedStateHandle());runCurrent()
  vm.onNameChange("draft");vm.saveChanges();vm.onAvatarSelected(mockk());runCurrent()
  assertFalse(vm.uiState.value.dataReady)
  coVerify(exactly=0) {users.updateProfile(any(),any(),any(),any())}
  coVerify(exactly=0) {images.persist(any())}
 }
 @Test fun `duplicate saves are gated synchronously and image exceptions keep the prior image`()=runTest {
  val users=mockk<UserRepository>(relaxUnitFun=true);val prefs=mockk<UserPreferences>(relaxUnitFun=true);val images=mockk<EhsanImageStore>()
  every {users.getUser()} returns flowOf(UserEntity(1,"Fixture","Name","","fixture",""))
  every {prefs.userAvatarUri} returns flowOf("ihsan-image:fixture.jpg");every {prefs.userBio} returns flowOf("")
  coEvery {users.updateProfile(any(),any(),any(),any())} coAnswers {delay(100)}
  coEvery {images.persist(any())} throws IOException()
  val vm=EditProfileViewModel(users,prefs,images,SavedStateHandle());runCurrent()
  vm.saveChanges();vm.saveChanges();advanceUntilIdle()
  coVerify(exactly=1) {users.updateProfile(any(),any(),any(),any())}
  vm.onAvatarSelected(mockk());vm.onAvatarSelected(mockk());runCurrent()
  coVerify(exactly=1) {images.persist(any())}
  assertFalse(vm.uiState.value.isLoading);assertEquals("ihsan-image:fixture.jpg",vm.uiState.value.avatarUrl)
 }
 @Test fun `initial failure supports retry without replacing restored user edits`()=runTest {
  val users=mockk<UserRepository>();val prefs=mockk<UserPreferences>();val images=mockk<EhsanImageStore>()
  var failed=true
  every { users.getUser() } answers { if(failed) flow { throw IOException() } else flowOf(UserEntity(1,"Saved","Name","","حلب","")) }
  every { prefs.userAvatarUri } returns flowOf(null);every { prefs.userBio } returns flowOf("bio")
  val handle=SavedStateHandle(mapOf("edit_profile_name" to "مسودة محفوظة"))
  val vm=EditProfileViewModel(users,prefs,images,handle);runCurrent();assertNotNull(vm.uiState.value.errorMessage);assertFalse(vm.uiState.value.isLoading)
  failed=false;vm.retry();runCurrent();assertEquals("مسودة محفوظة",vm.uiState.value.name);assertNull(vm.uiState.value.errorMessage)
 }
 @Test fun `save failure keeps draft and never signals success`()=runTest {
  val users=mockk<UserRepository>();val prefs=mockk<UserPreferences>();val images=mockk<EhsanImageStore>()
  every { users.getUser() } returns flowOf(UserEntity(1,"Saved","Name","","حلب",""))
  every { prefs.userAvatarUri } returns flowOf(null);every { prefs.userBio } returns flowOf("")
  coEvery { users.updateProfile(any(),any(),any(),any()) } throws IOException("private path")
  val vm=EditProfileViewModel(users,prefs,images,SavedStateHandle());runCurrent();vm.onNameChange("تعديل الاسم");vm.saveChanges();runCurrent()
  assertEquals("تعديل الاسم",vm.uiState.value.name);assertNull(vm.uiState.value.successMessage);assertNotNull(vm.uiState.value.errorMessage)
  assertFalse(vm.uiState.value.errorMessage!!.contains("private path"))
 }
}
