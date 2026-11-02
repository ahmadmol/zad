package com.example.feature.ehsan
import androidx.lifecycle.SavedStateHandle
import com.example.feature.ehsan.presentation.*
import com.example.feature.ehsan.data.local.dao.*
import com.example.feature.ehsan.data.local.entity.*
import com.example.feature.ehsan.data.repository.EhsanRepositoryImpl
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.domain.usecase.GetDonationsUseCase
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import java.io.IOException
import org.junit.*
import org.junit.Assert.*
@OptIn(ExperimentalCoroutinesApi::class)
class LocalCommunityRecoveryTest {
 private val dispatcher=StandardTestDispatcher()
 @Before fun before() { Dispatchers.setMain(dispatcher) }
 @After fun after() { Dispatchers.resetMain() }
 private fun row(origin:String="USER",owner:String?="local-profile:1")=DonationEntity(7,"عنوان","وصف","كتب","حلب","OFFER","AVAILABLE","اسم","",null,10,owner,origin)
 @Test fun `origin and stored owner block fabricated mutation inputs`()=runTest {
  val dao=mockk<DonationDao>(relaxed=true);val users=mockk<UserDao>()
  every { users.getUser() } returns flowOf(UserEntity(1,"a","b","","",""))
  val repo=EhsanRepositoryImpl(dao,users=users)
  for(stored in listOf(row("DEMO"),row("LEGACY",null),row(owner="local-profile:2"))) {
   coEvery { dao.getDonationById(7) } returns stored
   try { repo.deleteDonation(stored.toDomain().copy(origin="USER",ownerId="local-profile:1")); fail() } catch(_:SecurityException) { }
  }
  coVerify(exactly=0) { dao.deleteDonation(any()) }
 }
 @Test fun `after committed row deletion image cleanup failure is not reported as delete failure`()=runTest {
  val dao=mockk<DonationDao>(relaxed=true);val users=mockk<UserDao>();val images=mockk<EhsanImageStore>()
  every { users.getUser() } returns flowOf(UserEntity(1,"a","b","","",""))
  coEvery { dao.getDonationById(7) } returns row()
  every { dao.getAllDonations() } returns flowOf(emptyList())
  coEvery { images.deleteUnreferenced(any(),any()) } throws IOException()
  EhsanRepositoryImpl(dao,users=users,imageStore=images).deleteDonation(row().toDomain())
  coVerify(exactly=1) { dao.deleteDonation(row()) }
 }
 @Test fun `board restores filters retries storage and clears old error`()=runTest {
  val source=mockk<GetDonationsUseCase>();val location=mockk<PrayerLocationRepository>()
  every { location.observeLocation() } returns emptyFlow()
  var failed=true;every { source() } answers { if(failed) flow { throw IOException() } else flowOf(listOf(row().toDomain(),row("DEMO").toDomain().copy(id=8))) }
  val handle=SavedStateHandle();val vm=EhsanViewModel(source,location,savedState=handle)
  runCurrent();assertNotNull(vm.uiState.value.error)
  vm.onSearchQueryChange("عنوان");vm.onOriginChange("USER");vm.onStatusChange("AVAILABLE")
  failed=false;vm.retry();runCurrent();assertNull(vm.uiState.value.error);assertEquals(1,vm.uiState.value.filteredDonations.size)
  val restored=EhsanViewModel(source,location,savedState=handle);runCurrent()
  assertEquals("عنوان",restored.uiState.value.searchQuery);assertEquals("USER",restored.uiState.value.selectedOrigin)
 }
 @Test fun `details missing item clears prior item and old in-flight read cannot win`()=runTest {
  val repo=mockk<com.example.feature.ehsan.domain.repository.EhsanRepository>()
  coEvery { repo.getDonationById(1) } coAnswers { delay(1000);row().toDomain() }
  coEvery { repo.getDonationById(2) } returns null
  val vm=IhsanDetailsViewModel(repo);vm.loadItem(1);runCurrent();vm.loadItem(2);advanceTimeBy(1100);runCurrent()
  assertNull(vm.uiState.value.item);assertFalse(vm.uiState.value.isLoading);assertNotNull(vm.uiState.value.errorMessage)
 }
}
