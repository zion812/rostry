package com.rio.rostry.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.Chat
import com.rio.rostry.data.model.Message
import com.rio.rostry.data.model.MessageType
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.ChatRepository
import com.rio.rostry.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false,
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val currentUserId: String = "",
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    fun loadChat(chatId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val chat = chatRepository.getChatById(chatId)
                    val messages = chatRepository.getChatMessages(chatId)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        chat = chat,
                        messages = messages,
                        currentUserId = currentUser.uid
                    )
                    
                    // Mark messages as read
                    chatRepository.markMessagesAsRead(chatId, currentUser.uid)
                    
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load chat: ${e.message}"
                )
            }
        }
    }
    
    fun sendMessage(content: String) {
        val currentState = _uiState.value
        val chat = currentState.chat ?: return
        val currentUserId = currentState.currentUserId
        
        if (content.isBlank() || currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            try {
                // Get current user's display name
                val currentUser = authRepository.getCurrentUser()
                val senderName = currentUser?.displayName ?: "Unknown User"
                
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = chat.id,
                    senderId = currentUserId,
                    senderName = senderName,
                    content = content,
                    type = MessageType.TEXT,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                
                chatRepository.sendMessage(message)
                
                // Update local state immediately for better UX
                val updatedMessages = currentState.messages + message
                _uiState.value = currentState.copy(messages = updatedMessages)
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    error = "Failed to send message: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}