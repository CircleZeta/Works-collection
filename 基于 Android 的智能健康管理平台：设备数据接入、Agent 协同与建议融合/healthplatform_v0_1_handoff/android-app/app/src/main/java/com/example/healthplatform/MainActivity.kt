package com.example.healthplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.healthplatform.common.AppContainer
import com.example.healthplatform.ui.HealthPlatformApp
import com.example.healthplatform.ui.theme.HealthPlatformTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContainer = AppContainer(applicationContext)

        setContent {
            HealthPlatformTheme {
                HealthPlatformApp(appContainer = appContainer)
            }
        }
    }
}
