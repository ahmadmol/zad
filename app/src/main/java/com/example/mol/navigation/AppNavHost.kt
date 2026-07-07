package com.example.mol.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.feature.asma.presentation.AsmaScreen
import com.example.feature.azkar.AzkarScreen
import com.example.feature.azkar.presentation.AzkarViewModel
import com.example.feature.dashboard.DailyActivitiesScreen
import com.example.feature.dashboard.GlobalSearchScreen
import com.example.feature.dashboard.HomeDashboardScreen
import com.example.feature.duas.DuaDetailScreen
import com.example.feature.duas.DuaScreen
import com.example.feature.duas.presentation.DuaAction
import com.example.feature.duas.presentation.DuaViewModel
import com.example.feature.hadith.presentation.HadithScreen
import com.example.feature.hadith.presentation.HadithViewModel
import com.example.feature.ehsan.EhsanScreen
import com.example.feature.ehsan.presentation.AddEhsanScreen
import com.example.feature.ehsan.presentation.DonationDetailScreen
import com.example.feature.ehsan.presentation.IhsanDetailsScreen
import com.example.feature.ehsan.presentation.RequestHelpScreen
import com.example.feature.prayer.PrayerScreen
import com.example.feature.profile.DonationHistoryScreen
import com.example.feature.profile.ProfileScreen
import com.example.feature.profile.presentation.EditProfileScreen
import com.example.feature.qibla.presentation.QiblaScreen
import com.example.feature.quran.presentation.QuranAction
import com.example.feature.quran.presentation.QuranListScreen
import com.example.feature.quran.presentation.QuranReaderScreen
import com.example.feature.quran.presentation.QuranViewModel
import com.example.feature.reminders.RemindersScreen
import androidx.compose.runtime.LaunchedEffect
import com.example.feature.settings.SettingsScreen
import com.example.feature.dashboard.presentation.HomeDashboardAction
import com.example.feature.dashboard.presentation.HomeDashboardViewModel
import com.example.feature.statistics.StatisticsScreen
import com.example.feature.onboarding.OnboardingScreen
import com.example.feature.splashScreen.SplashScreen
import com.example.feature.ui.LocationPermissionScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToPermission = {
                    navController.navigate(Screen.LocationPermission.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.LocationPermission.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.LocationPermission.route) {
            LocationPermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.LocationPermission.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeDashboardScreen(
                onNavigateToQuran = { navController.navigate(Screen.Quran.route) },
                onNavigateToAzkar = { navController.navigate(Screen.Azkar.route) },
                onNavigateToHadith = { navController.navigate(Screen.Hadith.route) },
                onNavigateToDua = { navController.navigate(Screen.Dua.route) },
                onNavigateToDonations = { navController.navigate(Screen.Donations.route) },
                onNavigateToQibla = { navController.navigate(Screen.Qibla.route) },
                onNavigateToSearch = { navController.navigate(Screen.GlobalSearch.route) },
                onNavigateToPrayer = { navController.navigate(Screen.Prayer.route) },
                onNavigateToAsma = { navController.navigate(Screen.Asma.route) },
                onNavigateToDailyActivities = { navController.navigate(Screen.DailyActivities.route) }
            )
        }

        composable(route = Screen.GlobalSearch.route) {
            GlobalSearchScreen(
                onBack = { navController.popBackStack() },
                onNavigateToQuran = { surahId, ayah -> 
                    navController.navigate(Screen.QuranReader.createRoute(surahId, ayah))
                },
                onNavigateToDua = { duaId -> 
                    navController.navigate(Screen.DuaDetail.createRoute(duaId))
                },
                onNavigateToZikr = { /* Navigate to Zikr Pager or List */ }
            )
        }

        composable(route = Screen.DailyActivities.route) {
            val viewModel: HomeDashboardViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            DailyActivitiesScreen(
                activities = uiState.data.dailyActivities,
                onActivityIncrease = { id ->
                    viewModel.onAction(HomeDashboardAction.OnDailyActivityClick(id))
                },
                onActivityOpenRoute = { route ->
                    when (route) {
                        Screen.Qibla.route -> navController.navigate(Screen.Qibla.route)
                        Screen.Quran.route -> navController.navigate(Screen.Quran.route)
                        Screen.Azkar.route -> navController.navigate(Screen.Azkar.route)
                        Screen.Dua.route -> navController.navigate(Screen.Dua.route)
                        Screen.Asma.route -> navController.navigate(Screen.Asma.route)
                        else -> {}
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Qibla.route) {
            QiblaScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.Asma.route) {
            AsmaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Prayer.route) {
            PrayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Quran.route) {
            val viewModel: QuranViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            QuranListScreen(
                state = uiState,
                onSurahClick = { surahId, ayahNumber ->
                    navController.navigate(Screen.QuranReader.createRoute(surahId, ayahNumber))
                },
                onSearch = { query ->
                    viewModel.onAction(QuranAction.Search(query))
                },
                onClearSearch = {
                    viewModel.onAction(QuranAction.ClearSearch)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.QuranReader.route,
            arguments = listOf(
                navArgument("surahId") { type = NavType.IntType },
                navArgument("ayahNumber") { 
                    type = NavType.IntType
                    defaultValue = -1 
                }
            )
        ) { backStackEntry ->
            val surahId = backStackEntry.arguments?.getInt("surahId") ?: 1
            val ayahNumberArg = backStackEntry.arguments?.getInt("ayahNumber") ?: -1
            val ayahNumber = if (ayahNumberArg == -1) null else ayahNumberArg
            
            val viewModel: QuranViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            
            LaunchedEffect(surahId, ayahNumber) {
                viewModel.onAction(QuranAction.SelectSurah(surahId, ayahNumber))
            }
            
            QuranReaderScreen(
                state = uiState,
                onBack = { navController.popBackStack() },
                onRetry = { viewModel.onAction(QuranAction.SelectSurah(surahId, ayahNumber)) },
                onSaveLastRead = { sId, aNum ->
                    viewModel.onAction(QuranAction.SaveLastRead(sId, aNum))
                },
                onToggleBookmark = { sId, vNum ->
                    viewModel.onAction(QuranAction.ToggleBookmark(sId, vNum))
                },
                onTogglePlay = { viewModel.onAction(QuranAction.TogglePlay) },
                onPlayNext = { viewModel.onAction(QuranAction.PlayNext) },
                onPlayPrevious = { viewModel.onAction(QuranAction.PlayPrevious) },
                onDownloadSurah = { viewModel.onAction(QuranAction.DownloadSurah) },
                onSeekTo = { viewModel.onAction(QuranAction.SeekTo(it)) },
                onUpdateFontSize = { viewModel.onAction(QuranAction.UpdateFontSize(it)) }
            )
        }

        composable(route = Screen.Azkar.route) {
            val viewModel: AzkarViewModel = koinViewModel()
            AzkarScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Hadith.route) {
            val viewModel: HadithViewModel = koinViewModel()
            HadithScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Dua.route) {
            val viewModel: DuaViewModel = koinViewModel()
            DuaScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onDuaClick = { duaId ->
                    navController.navigate(Screen.DuaDetail.createRoute(duaId))
                }
            )
        }

        composable(
            route = Screen.DuaDetail.route,
            arguments = listOf(navArgument("duaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val duaId = backStackEntry.arguments?.getLong("duaId") ?: 0L
            val viewModel: DuaViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val dua = uiState.allDuas.find { it.id == duaId }
            
            DuaDetailScreen(
                dua = dua,
                onBack = { navController.popBackStack() },
                onToggleFavorite = { id, isFav ->
                    viewModel.onAction(DuaAction.OnToggleFavorite(id, isFav))
                }
            )
        }

        composable(route = Screen.Settings.route) {
            val viewModel: AzkarViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                state = uiState,
                onAction = viewModel::onAction,
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Statistics.route) {
            val viewModel: AzkarViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            StatisticsScreen(state = uiState, onBack = { navController.popBackStack() })
        }

        composable(route = Screen.Reminders.route) {
            RemindersScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.Donations.route) {
            EhsanScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddEhsanClick = { type -> 
                    if (type == "REQUEST") {
                        navController.navigate(Screen.RequestHelp.route)
                    } else {
                        navController.navigate(Screen.AddDonation.createRoute(type))
                    }
                },
                onDonationClick = { id -> 
                    navController.navigate(Screen.IhsanDetails.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.AddDonation.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "OFFER"
            AddEhsanScreen(
                onNavigateBack = { navController.popBackStack() },
                initialType = type
            )
        }

        composable(route = Screen.RequestHelp.route) {
            RequestHelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DonationDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val donationId = backStackEntry.arguments?.getLong("id") ?: 0L
            DonationDetailScreen(
                donationId = donationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.IhsanDetails.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            IhsanDetailsScreen(
                id = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToDonationHistory = { navController.navigate(Screen.DonationHistory.route) }
            )
        }

        composable(route = Screen.DonationHistory.route) {
            DonationHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
