package com.icare.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icare.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val resetEmailSent: Boolean = false,
    val recoveryEmail: String? = null,
    // OTP flow states
    val otpSent: Boolean = false,
    val otpVerified: Boolean = false,
    val pendingSignUpData: PendingSignUpData? = null,
    // Password reset OTP flow
    val resetOtpSent: Boolean = false,
    val resetOtpVerified: Boolean = false,
    val pendingResetEmail: String? = null
)

data class PendingSignUpData(
    val emailOrPhone: String,
    val displayName: String,
    val passcode: String,
    val recoveryEmail: String,
    val verificationEmail: String  // Email where OTP was sent
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(emailOrPhone: String, passcode: String) {
        if (emailOrPhone.isBlank() || passcode.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        if (passcode.length < 4 || passcode.length > 6) {
            _uiState.value = _uiState.value.copy(error = "Passcode must be 4-6 digits")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.login(emailOrPhone, passcode)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Login failed"
                    )
                }
        }
    }

    fun signUp(
        emailOrPhone: String,
        displayName: String,
        passcode: String,
        confirmPasscode: String,
        recoveryEmail: String = ""
    ) {
        if (emailOrPhone.isBlank() || displayName.isBlank() || passcode.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        if (passcode.length < 4 || passcode.length > 6) {
            _uiState.value = _uiState.value.copy(error = "Passcode must be 4-6 digits")
            return
        }

        if (passcode != confirmPasscode) {
            _uiState.value = _uiState.value.copy(error = "Passcodes don't match")
            return
        }

        val isPhoneUser = !emailOrPhone.contains("@")
        if (isPhoneUser && recoveryEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Recovery email is required for phone sign-up")
            return
        }

        if (isPhoneUser && !recoveryEmail.contains("@")) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid recovery email")
            return
        }

        // Determine which email to verify
        val verificationEmail = if (isPhoneUser) recoveryEmail else emailOrPhone

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Send OTP to the verification email
            authRepository.sendOtp(verificationEmail, displayName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otpSent = true,
                        pendingSignUpData = PendingSignUpData(
                            emailOrPhone = emailOrPhone,
                            displayName = displayName,
                            passcode = passcode,
                            recoveryEmail = recoveryEmail,
                            verificationEmail = verificationEmail
                        )
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to send verification code"
                    )
                }
        }
    }

    fun verifyOtpAndSignUp(otp: String) {
        val pendingData = _uiState.value.pendingSignUpData
        if (pendingData == null) {
            _uiState.value = _uiState.value.copy(error = "No pending sign-up data")
            return
        }

        if (otp.length != 4) {
            _uiState.value = _uiState.value.copy(error = "Please enter a 4-digit code")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Verify OTP
            authRepository.verifyOtp(pendingData.verificationEmail, otp)
                .onSuccess { verified ->
                    if (verified) {
                        // OTP verified, now create the account
                        authRepository.signUpAfterOtpVerification(
                            pendingData.emailOrPhone,
                            pendingData.passcode,
                            pendingData.displayName,
                            pendingData.recoveryEmail
                        ).onSuccess {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                otpVerified = true,
                                isSuccess = true
                            )
                        }.onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to create account"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Invalid verification code"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Verification failed"
                    )
                }
        }
    }

    fun resendOtp() {
        val pendingData = _uiState.value.pendingSignUpData ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.sendOtp(pendingData.verificationEmail, pendingData.displayName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to resend code"
                    )
                }
        }
    }

    fun cancelOtpVerification() {
        _uiState.value = _uiState.value.copy(
            otpSent = false,
            pendingSignUpData = null,
            error = null
        )
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signInWithGoogle(idToken)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Google sign-in failed"
                    )
                }
        }
    }

    fun sendPasswordResetOtp(emailOrPhone: String) {
        if (emailOrPhone.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your email")
            return
        }

        // Only accept email addresses for password reset
        if (!emailOrPhone.contains("@")) {
            _uiState.value = _uiState.value.copy(error = "Please enter your email address (not phone number)")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Cloud function will look up user by email OR recoveryEmail
                authRepository.sendPasswordResetOtp(emailOrPhone)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            resetOtpSent = true,
                            pendingResetEmail = emailOrPhone
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to send reset code"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred. Please try again."
                )
            }
        }
    }

    fun verifyResetOtpAndChangePassword(otp: String, newPassword: String, confirmPassword: String) {
        val email = _uiState.value.pendingResetEmail
        if (email.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(error = "No pending reset request")
            return
        }

        if (otp.length != 4) {
            _uiState.value = _uiState.value.copy(error = "Please enter a 4-digit code")
            return
        }

        if (newPassword.length < 4 || newPassword.length > 6) {
            _uiState.value = _uiState.value.copy(error = "Passcode must be 4-6 digits")
            return
        }

        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Passcodes don't match")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.resetPasswordWithOtp(email, otp, newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resetOtpVerified = true
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to reset passcode"
                    )
                }
        }
    }

    fun resendResetOtp() {
        val email = _uiState.value.pendingResetEmail ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.sendPasswordResetOtp(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to resend code"
                    )
                }
        }
    }

    fun cancelPasswordReset() {
        _uiState.value = _uiState.value.copy(
            resetOtpSent = false,
            resetOtpVerified = false,
            pendingResetEmail = null,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
