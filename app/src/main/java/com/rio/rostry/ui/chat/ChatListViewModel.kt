package com.rio.rostry.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.Chat
import com.rio.rostry.data.model.User
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.ChatRepository
import com.rio.rostry.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val isLoading: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val chatParticipants: Map<String, User> = emptyMap(), // Map of userId to User
    val currentUserId: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<User> = emptyList(),
    val showSearchResults: Boolean = false,
    val recentUsers: List<User> = emptyList(),
    val error: String? = null,
    val isStartingChat: Boolean = false
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()
    
    fun loadChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val chats = chatRepository.getUserChats(currentUser.uid)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        chats = chats,
                        currentUserId = currentUser.uid
                    )
                    
                    // Load participant information for all chats
                    loadChatParticipants(chats, currentUser.uid)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load chats: ${e.message}"
                )
            }
        }
    }
    
    private fun loadChatParticipants(chats: List<Chat>, currentUserId: String) {
        viewModelScope.launch {
            try {
                // Get all unique participant IDs (excluding current user)
                val participantIds = chats.flatMap { chat ->
                    chat.participants.filter { it != currentUserId }
                }.distinct()
                
                if (participantIds.isNotEmpty()) {
                    // Load user information for all participants
                    val participants = userRepository.getUsersByIds(participantIds)
                    val participantMap = participants.associateBy { it.id }
                    
                    _uiState.value = _uiState.value.copy(
                        chatParticipants = participantMap
                    )
                }
            } catch (e: Exception) {
                // Silently fail for participant loading
                // The UI will show fallback names
            }
        }
    }
    
    fun loadRecentUsers() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val recentUsers = userRepository.getRecentUsers(10)
                        .filter { it.id != currentUser.uid } // Exclude current user
                    _uiState.value = _uiState.value.copy(recentUsers = recentUsers)
                }
            } catch (e: Exception) {
                // Silently fail for recent users
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            showSearchResults = query.isNotBlank()
        )
        
        if (query.isNotBlank()) {
            searchUsers(query)
        } else {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                showSearchResults = false
            )
        }
    }
    
    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val results = userRepository.searchUsers(query)
                        .filter { it.id != currentUser.uid } // Exclude current user
                    
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = results
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = "Failed to search users: ${e.message}"
                )
            }
        }
    }
    
    fun startChatWithUser(user: User, onChatCreated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isStartingChat = true, error = null)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val result = chatRepository.createOrGetChatFirebase(
                        user1Id = currentUser.uid,
                        user2Id = user.id,
                        user1Name = currentUser.displayName ?: "User",
                        user2Name = user.displayName
                    )
                    
                    result.fold(
                        onSuccess = { chatId ->
                            _uiState.value = _uiState.value.copy(isStartingChat = false)
                            onChatCreated(chatId)
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isStartingChat = false,
                                error = "Failed to start chat: ${exception.message}"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isStartingChat = false,
                        error = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isStartingChat = false,
                    error = "Failed to start chat: ${e.message}"
                )
            }
        }
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            showSearchResults = false
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getChatParticipantName(chat: Chat, currentUserId: String): String {
        // Find the other participant (not the current user)
        val otherParticipantId = chat.participants.find { it != currentUserId }
        
        return if (otherParticipantId != null) {
            // Get the user from the participants map
            val participant = _uiState.value.chatParticipants[otherParticipantId]
            participant?.displayName?.ifEmpty { "Unknown User" } ?: "Unknown User"
        } else {
            "Unknown User"
        }
    }
    
    fun getChatParticipantProfileImage(chat: Chat, currentUserId: String): String {
        // Find the other participant (not the current user)
        val otherParticipantId = chat.participants.find { it != currentUserId }
        
        return if (otherParticipantId != null) {
            // Get the user from the participants map
            val participant = _uiState.value.chatParticipants[otherParticipantId]
            participant?.profileImageUrl ?: ""
        } else {
            ""
        }
    }
}