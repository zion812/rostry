package com.rio.rostry.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.Post
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CreatePostUiState(
    val isLoading: Boolean = false,
    val content: String = "",
    val selectedImageUri: String = "",
    val postType: String = "general",
    val location: String = "",
    val error: String? = null
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()
    
    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content, error = null)
    }
    
    fun updateSelectedImageUri(uri: String) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri, error = null)
    }
    
    fun updatePostType(type: String) {
        _uiState.value = _uiState.value.copy(postType = type, error = null)
    }
    
    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location, error = null)
    }
    
    fun createPost(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        if (currentState.content.isBlank()) {
            _uiState.value = currentState.copy(error = "Post content cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val post = Post(
                        id = UUID.randomUUID().toString(),
                        authorId = currentUser.uid,
                        authorName = currentUser.displayName,
                        authorProfileImage = currentUser.profileImageUrl,
                        content = currentState.content.trim(),
                        imageUrls = if (currentState.selectedImageUri.isNotEmpty()) {
                            listOf(currentState.selectedImageUri)
                        } else {
                            emptyList()
                        },
                        likes = emptyList(),
                        comments = emptyList(),
                        location = currentState.location.trim(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    postRepository.createPost(post)
                    
                    _uiState.value = currentState.copy(isLoading = false)
                    onSuccess()
                    
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "Failed to create post: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}