package com.hostelhub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.hostelhub.data.repository.AuthRepository
import com.hostelhub.ui.navigation.HostelHubNavGraph
import com.hostelhub.ui.theme.HostelHubTheme
import com.hostelhub.ui.theme.ThemePreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            val isDarkPref by ThemePreferenceManager.getThemeModeFlow(this).collectAsState(initial = null)
            val darkTheme = isDarkPref ?: isSystemInDarkTheme()
            
            HostelHubTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    HostelHubNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "hostelhub" && uri.host == "oauth") {
            val accessToken = uri.getQueryParameter("accessToken")
            val refreshToken = uri.getQueryParameter("refreshToken")
            val userId = uri.getQueryParameter("userId")
            if (accessToken != null && refreshToken != null && userId != null) {
                lifecycleScope.launch {
                    authRepository.saveOAuthTokens(accessToken, refreshToken, userId)
                }
            }
        }
    }
}
