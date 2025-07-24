package com.rio.rostry.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val emailError: String? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            error = null,
            successMessage = null
        )
    }
    
    fun sendPasswordReset(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        // Validate email
        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(emailError = "Email is required")
            return
        }
        
        if (!isValidEmail(currentState.email)) {
            _uiState.value = currentState.copy(emailError = "Please enter a valid email address")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                emailError = null,
                error = null,
                successMessage = null
            )
            
            try {
                authRepository.sendPasswordResetEmail(currentState.email)
                
                _uiState.value = currentState.copy(
                    isLoading = false,
                    successMessage = "Password reset email sent successfully!"
                )
                
                // Delay before calling onSuccess to show the success message
                kotlinx.coroutines.delay(2000)
                onSuccess()
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = when {
                        e.message?.contains("user-not-found") == true -> 
                            "No account found with this email address"
                        e.message?.contains("invalid-email") == true -> 
                            "Please enter a valid email address"
                        e.message?.contains("too-many-requests") == true -> 
                            "Too many requests. Please try again later"
                        else -> "Failed to send reset email: ${e.message}"
                    }
                )
            }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}