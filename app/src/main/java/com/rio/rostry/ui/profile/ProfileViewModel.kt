package com.rio.rostry.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.User
import com.rio.rostry.data.model.UserRole
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.data.repository.PostRepository
import com.rio.rostry.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val fowlCount: Int = 0,
    val postCount: Int = 0,
    val followingCount: Int = 0,
    val memberSince: String = "",
    val isLoggedOut: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val fowlRepository: FowlRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    // Load fowl count
                    val fowls = fowlRepository.getUserFowls(currentUser.id)
                    val fowlCount = fowls.size
                    
                    // Load post count
                    val userPosts = postRepository.getUserPosts(currentUser.id)
                    val postCount = userPosts.size
                    
                    // Load following count (for now, use a placeholder)
                    // In a real implementation, you would have a following/followers system
                    val followingCount = currentUser.followingIds?.size ?: 0
                    
                    // Format member since date
                    val memberSince = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                        .format(Date(currentUser.createdAt))
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = currentUser,
                        fowlCount = fowlCount,
                        postCount = postCount,
                        followingCount = followingCount,
                        memberSince = memberSince
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.value = _uiState.value.copy(isLoggedOut = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to logout: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun updateProfile(
        displayName: String,
        phoneNumber: String,
        location: String,
        bio: String,
        role: UserRole,
        profileImageUri: Uri?,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    // TODO: Handle image upload if profileImageUri is provided
                    val imageUrl = if (profileImageUri != null) {
                        // For now, keep the existing image URL
                        // In a real implementation, you would upload the image to Firebase Storage
                        currentUser.profileImageUrl
                    } else {
                        currentUser.profileImageUrl
                    }
                    
                    val updatedUser = currentUser.copy(
                        displayName = displayName,
                        phoneNumber = phoneNumber,
                        location = location,
                        bio = bio,
                        role = role,
                        profileImageUrl = imageUrl
                    )
                    
                    val result = authRepository.updateUserProfile(updatedUser)
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(user = updatedUser)
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}