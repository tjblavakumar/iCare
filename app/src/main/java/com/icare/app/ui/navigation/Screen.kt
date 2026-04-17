package com.icare.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Permissions : Screen("permissions")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object OtpVerification : Screen("otp_verification")
    object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")
    data object Circle : Screen("circle")
    data object Notifications : Screen("notifications")
    data object Settings : Screen("settings")
    data object ManageContacts : Screen("manage_contacts")
    data object PendingRequests : Screen("pending_requests")
    data object AddContact : Screen("add_contact")
    data object ContactHistory : Screen("contact_history/{contactId}") {
        fun createRoute(contactId: String): String = "contact_history/$contactId"
    }
}
