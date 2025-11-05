package com.enbridge.gpsdeviceproj.navigation

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
import com.enbridge.gpsdeviceproj.BuildConfig
import com.enbridge.gpsdeviceproj.ui.auth.LoginScreen
import com.enbridge.gpsdeviceproj.ui.map.MainMapScreen
import com.enbridge.gpsdeviceproj.ui.jobs.JobCardEntryScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Map : Screen("map")
    object JobCardEntry : Screen("job_card_entry")
}

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
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to map and clear back stack
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
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
            MainMapScreen(
                onNavigateToJobCardEntry = {
                    navController.navigate(Screen.JobCardEntry.route)
                },
                onAddNewFeature = {
                    // Handle add new feature
                },
                onSearchClick = {
                    // Handle search click
                },
                onLogout = {
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
            JobCardEntryScreen(
                onClose = {
                    navController.popBackStack()
                }
            )
        }
    }
}
