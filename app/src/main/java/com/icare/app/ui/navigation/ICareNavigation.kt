package com.icare.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.hilt.navigation.compose.hiltViewModel
import com.icare.app.di.dataStore
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.icare.app.ui.MainViewModel
import com.icare.app.ui.components.BottomNavBar
import com.icare.app.ui.screens.auth.AuthViewModel
import com.icare.app.ui.screens.auth.ForgotPasswordScreen
import com.icare.app.ui.screens.auth.LoginScreen
import com.icare.app.ui.screens.auth.OtpVerificationScreen
import com.icare.app.ui.screens.auth.SignUpScreen
import com.icare.app.ui.screens.circle.CircleScreen
import com.icare.app.ui.screens.circle.ContactHistoryScreen
import com.icare.app.ui.screens.home.HomeScreen
import com.icare.app.ui.screens.notifications.NotificationsScreen
import com.icare.app.ui.screens.onboarding.OnboardingScreen
import com.icare.app.ui.screens.permissions.PermissionsScreen
import com.icare.app.ui.screens.settings.AddContactScreen
import com.icare.app.ui.screens.settings.ManageContactsScreen
import com.icare.app.ui.screens.settings.PendingRequestsScreen
import com.icare.app.ui.screens.settings.SettingsScreen

@Composable
fun ICareNavigation(
    startDestination: String,
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pendingRequestsCount by mainViewModel.pendingRequestsCount.collectAsState()

    // Refresh pending count when navigating to main screens
    LaunchedEffect(currentRoute) {
        if (currentRoute in listOf(Screen.Home.route, Screen.Circle.route, Screen.Notifications.route, Screen.Settings.route)) {
            mainViewModel.loadPendingRequestsCount()
        }
    }

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
                    },
                    pendingRequestsCount = pendingRequestsCount
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

            composable(Screen.SignUp.route) { backStackEntry ->
                // Get parent entry to share ViewModel with OTP screen
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.SignUp.route)
                }
                val sharedViewModel: AuthViewModel = hiltViewModel(parentEntry)
                
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.Permissions.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    },
                    onNavigateToOtp = {
                        navController.navigate(Screen.OtpVerification.route)
                    },
                    viewModel = sharedViewModel
                )
            }

            composable(Screen.OtpVerification.route) {
                // Get the SignUp screen's back stack entry to share ViewModel
                val parentEntry = remember {
                    navController.getBackStackEntry(Screen.SignUp.route)
                }
                val sharedViewModel: AuthViewModel = hiltViewModel(parentEntry)
                
                OtpVerificationScreen(
                    onVerificationSuccess = {
                        navController.navigate(Screen.Permissions.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    viewModel = sharedViewModel
                )
            }

            composable(Screen.Permissions.route) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                PermissionsScreen(
                    onComplete = {
                        scope.launch {
                            context.dataStore.edit { prefs ->
                                prefs[booleanPreferencesKey("permissions_shown")] = true
                            }
                        }
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Permissions.route) { inclusive = true }
                        }
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
                    },
                    onAddContactClick = {
                        navController.navigate(Screen.AddContact.route)
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
