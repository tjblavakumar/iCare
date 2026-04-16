package com.icare.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.icare.app.ui.components.BottomNavBar
import com.icare.app.ui.screens.auth.ForgotPasswordScreen
import com.icare.app.ui.screens.auth.LoginScreen
import com.icare.app.ui.screens.auth.SignUpScreen
import com.icare.app.ui.screens.circle.CircleScreen
import com.icare.app.ui.screens.circle.ContactHistoryScreen
import com.icare.app.ui.screens.home.HomeScreen
import com.icare.app.ui.screens.notifications.NotificationsScreen
import com.icare.app.ui.screens.onboarding.OnboardingScreen
import com.icare.app.ui.screens.settings.AddContactScreen
import com.icare.app.ui.screens.settings.ManageContactsScreen
import com.icare.app.ui.screens.settings.PendingRequestsScreen
import com.icare.app.ui.screens.settings.SettingsScreen

@Composable
fun ICareNavigation(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Circle.route,
        Screen.Notifications.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Screen.SignUp.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen()
            }

            composable(Screen.Circle.route) {
                CircleScreen(
                    onContactClick = { contactId ->
                        navController.navigate(Screen.ContactHistory.createRoute(contactId))
                    }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToManageContacts = {
                        navController.navigate(Screen.ManageContacts.route)
                    },
                    onNavigateToPendingRequests = {
                        navController.navigate(Screen.PendingRequests.route)
                    },
                    onNavigateToAddContact = {
                        navController.navigate(Screen.AddContact.route)
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ManageContacts.route) {
                ManageContactsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PendingRequests.route) {
                PendingRequestsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddContact.route) {
                AddContactScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ContactHistory.route,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType })
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId") ?: return@composable
                ContactHistoryScreen(
                    contactId = contactId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
