package com.example.healthplatform.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthplatform.common.AppContainer
import com.example.healthplatform.presentation.advice.AdviceScreen
import com.example.healthplatform.presentation.advice.AdviceViewModel
import com.example.healthplatform.presentation.advice.AdviceViewModelFactory
import com.example.healthplatform.presentation.dashboard.DashboardScreen
import com.example.healthplatform.presentation.dashboard.DashboardViewModel
import com.example.healthplatform.presentation.dashboard.DashboardViewModelFactory
import com.example.healthplatform.presentation.navigation.Screen
import com.example.healthplatform.presentation.navigation.bottomNavScreens
import com.example.healthplatform.presentation.settings.SettingsScreen
import com.example.healthplatform.presentation.trends.TrendsScreen
import com.example.healthplatform.presentation.trends.TrendsViewModel
import com.example.healthplatform.presentation.trends.TrendsViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HealthPlatformApp(appContainer: AppContainer) {
    val refreshTick by appContainer.refreshTicker.refreshTick.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { bottomNavScreens.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            getDashboardSummaryUseCase = appContainer.getDashboardSummaryUseCase
        )
    )
    val trendsViewModel: TrendsViewModel = viewModel(
        factory = TrendsViewModelFactory(
            getTrendDataUseCase = appContainer.getTrendDataUseCase
        )
    )
    val adviceViewModel: AdviceViewModel = viewModel(
        factory = AdviceViewModelFactory(
            generateSuggestionsUseCase = appContainer.generateSuggestionsUseCase,
            importMockDataUseCase = appContainer.importMockDataUseCase,
            suggestionRepository = appContainer.suggestionRepository,
            refreshTicker = appContainer.refreshTicker
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            if (pagerState.currentPage != index) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        icon = {
                            Text(
                                text = screen.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (bottomNavScreens[page]) {
                Screen.Dashboard -> {
                    DashboardScreen(
                        contentPadding = innerPadding,
                        viewModel = dashboardViewModel,
                        refreshTick = refreshTick
                    )
                }
                Screen.Trends -> {
                    TrendsScreen(
                        contentPadding = innerPadding,
                        viewModel = trendsViewModel,
                        refreshTick = refreshTick
                    )
                }
                Screen.Advice -> {
                    AdviceScreen(
                        contentPadding = innerPadding,
                        viewModel = adviceViewModel
                    )
                }
                Screen.Settings -> {
                    SettingsScreen(contentPadding = innerPadding)
                }
            }
        }
    }
}
