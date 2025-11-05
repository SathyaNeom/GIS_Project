package com.enbridge.gdsgpscollection

/**
 * @author Sathya Narayanan
 */
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.navigation.AppNavGraph
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.i(TAG, "MainActivity onCreate - Starting main activity")
        Logger.d(TAG, "Build variant: ${BuildConfig.APP_VARIANT}")
        Logger.d(TAG, "App name: ${BuildConfig.APP_NAME}")

        // Enable edge-to-edge display
        enableEdgeToEdge()
        Logger.d(TAG, "Edge-to-edge display enabled")

        setContent {
            Logger.d(TAG, "Setting up Compose UI")
            GdsGpsCollectionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    Logger.d(TAG, "Navigation controller initialized")
                    AppNavGraph(navController = navController)
                }
            }
        }

        Logger.i(TAG, "MainActivity onCreate completed")
    }

    override fun onStart() {
        super.onStart()
        Logger.d(TAG, "MainActivity onStart - Activity becoming visible")
    }

    override fun onResume() {
        super.onResume()
        Logger.d(TAG, "MainActivity onResume - Activity in foreground")
    }

    override fun onPause() {
        super.onPause()
        Logger.d(TAG, "MainActivity onPause - Activity going to background")
    }

    override fun onStop() {
        super.onStop()
        Logger.d(TAG, "MainActivity onStop - Activity no longer visible")
    }

    override fun onDestroy() {
        Logger.i(TAG, "MainActivity onDestroy - Activity being destroyed")
        super.onDestroy()
    }
}
