package com.example.healthplatform.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    viewModel: DashboardViewModel,
    refreshTick: Long
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = "demo_user"
    var lastLoadedRefreshTick by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(refreshTick) {
        if (lastLoadedRefreshTick != refreshTick) {
            lastLoadedRefreshTick = refreshTick
            viewModel.load(
                userId = userId,
                start = startOfToday(),
                end = System.currentTimeMillis()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineSmall
        )

        if (uiState.loading) {
            CircularProgressIndicator()
        }

        uiState.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        DashboardCard(title = "Today's Steps", value = uiState.todaySteps)
        DashboardCard(title = "Average Heart Rate", value = uiState.avgHeartRate)
        DashboardCard(title = "Sleep Duration", value = uiState.sleepDuration)
        DashboardCard(title = "Status Summary", value = uiState.statusSummary)
    }
}

@Composable
private fun DashboardCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun startOfToday(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
