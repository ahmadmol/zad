package com.example.feature.learning

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.core.network.ConnectivityStatus
import com.example.feature.fahmanallah.data.repository.FahmRepositoryImpl
import com.example.feature.fahmanallah.presentation.FahmCompletionScreen
import com.example.feature.fahmanallah.presentation.FahmViewModel
import com.example.feature.nabiihsan.data.repository.NabiIhsanRepositoryImpl
import com.example.feature.nabiihsan.domain.repository.NabiIhsanRepository
import com.example.feature.nabiihsan.presentation.NabiIhsanViewModel
import com.example.feature.nabiihsan.presentation.NabiIhsanWatchLaterScreen
import kotlinx.coroutines.flow.*
import org.junit.Rule
import org.junit.Test

/** In-memory catalogs only; compiling this class does not execute Android acceptance. */
class LearningRecoveryUiTest {
    @get:Rule val rule=createComposeRule()
    private val offline=object:ConnectivityMonitor {
        override val status=MutableStateFlow(ConnectivityStatus.Offline)
        override val isOnline=MutableStateFlow(false)
    }
    @Test fun uncompletedLessonDoesNotRenderACompletionClaim() {
        rule.setContent {
            val vm=androidx.lifecycle.viewmodel.compose.viewModel<FahmViewModel>(factory=object:androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST") override fun <T:androidx.lifecycle.ViewModel> create(modelClass:Class<T>):T =
                    FahmViewModel(FahmRepositoryImpl(),offline) as T
            })
            IhsanTheme {FahmCompletionScreen("ep_01",vm,{},{},{})}
        }
        rule.onNodeWithText("لم تسجل إتمام هذا الدرس").assertExists()
        rule.onNodeWithText("أحسنت!").assertDoesNotExist()
        rule.onNodeWithText("فتح الدرس").assertHasClickAction()
    }
    @Test fun aFailedWatchLaterReadIsAnErrorInsteadOfAnEmptyLibrary() {
        val repo=object:NabiIhsanRepository by NabiIhsanRepositoryImpl() {
            override fun observeEpisodes()=flow<List<com.example.feature.nabiihsan.domain.model.NabiIhsanEpisode>> {
                throw java.io.IOException("synthetic fixture")
            }
        }
        rule.setContent {
            val vm=androidx.lifecycle.viewmodel.compose.viewModel<NabiIhsanViewModel>(factory=object:androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST") override fun <T:androidx.lifecycle.ViewModel> create(modelClass:Class<T>):T=NabiIhsanViewModel(repo) as T
            })
            IhsanTheme {NabiIhsanWatchLaterScreen(vm)}
        }
        rule.onNodeWithText("تعذر قراءة مكتبة نبي الإحسان").assertExists()
        rule.onNodeWithText("قائمة المشاهدة لاحقاً فارغة").assertDoesNotExist()
    }
}
