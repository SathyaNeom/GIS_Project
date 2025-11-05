package com.enbridge.gdsgpscollection.navigation

/**
 * @author Sathya Narayanan
 */

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.enbridge.gdsgpscollection.BuildConfig
import com.enbridge.gdsgpscollection.ui.auth.LoginScreen
import com.enbridge.gdsgpscollection.ui.map.MainMapScreen
import com.enbridge.gdsgpscollection.ui.jobs.JobCardEntryScreen
import com.enbridge.gdsgpscollection.util.Logger

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Map : Screen("map")
    object JobCardEntry : Screen("job_card_entry")
}

private const val TAG = "Navigation"
private const val ANIMATION_DURATION = 350

private val slideAnimationSpec = tween<IntOffset>(
    durationMillis = ANIMATION_DURATION,
    easing = FastOutSlowInEasing
)

private val fadeAnimationSpec = tween<Float>(
    durationMillis = ANIMATION_DURATION,
    easing = FastOutSlowInEasing
)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Logger.d(TAG, "Initializing AppNavGraph with start destination: ${Screen.Login.route}")

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Login.route,
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = slideAnimationSpec
                ) + fadeOut(animationSpec = fadeAnimationSpec)
            }
        ) {
            Logger.i(TAG, "Screen: Login - User on login screen")
            LoginScreen(
                onLoginSuccess = {
                    Logger.i(TAG, "Navigation: Login -> Map (Login successful)")
                    // Navigate to map and clear back stack
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    Logger.d(TAG, "User clicked: Forgot Password")
                    // Handle forgot password
                }
            )
        }

        composable(
            route = Screen.Map.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = slideAnimationSpec
                ) + fadeIn(animationSpec = fadeAnimationSpec)
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = slideAnimationSpec
                ) + fadeOut(animationSpec = fadeAnimationSpec)
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = slideAnimationSpec
                ) + fadeIn(animationSpec = fadeAnimationSpec)
            }
        ) {
            Logger.i(TAG, "Screen: MainMap - User on main map screen")
            MainMapScreen(
                onNavigateToJobCardEntry = {
                    Logger.i(TAG, "Navigation: Map -> JobCardEntry")
                    navController.navigate(Screen.JobCardEntry.route)
                },
                onAddNewFeature = {
                    Logger.d(TAG, "User action: Add New Feature clicked")
                    // Handle add new feature
                },
                onSearchClick = {
                    Logger.d(TAG, "User action: Search clicked")
                    // Handle search click
                },
                onLogout = {
                    Logger.i(TAG, "Navigation: Map -> Login (User logged out)")
                    // Navigate back to login and clear the back stack
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Map.route) { inclusive = true }
                    }
                },
                appName = BuildConfig.APP_NAME,
                appVariant = BuildConfig.APP_VARIANT
            )
        }

        composable(
            route = Screen.JobCardEntry.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = slideAnimationSpec
                ) + fadeIn(animationSpec = fadeAnimationSpec)
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = slideAnimationSpec
                ) + fadeOut(animationSpec = fadeAnimationSpec)
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = slideAnimationSpec
                ) + fadeOut(animationSpec = fadeAnimationSpec)
            }
        ) {
            Logger.i(TAG, "Screen: JobCardEntry - User on job card entry screen")
            JobCardEntryScreen(
                onClose = {
                    Logger.i(TAG, "Navigation: JobCardEntry -> Map (Back to map)")
                    navController.popBackStack()
                }
            )
        }
    }
}
