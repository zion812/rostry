package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.ChatDao
import com.rio.rostry.data.local.dao.MessageDao
import com.rio.rostry.data.model.Chat
import com.rio.rostry.data.model.Message
import com.rio.rostry.data.model.MessageType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    
    suspend fun getUserChats(userId: String): List<Chat> {
        return chatDao.getUserChats(userId)
    }
    
    suspend fun getChatById(chatId: String): Chat? {
        return chatDao.getChatById(chatId)
    }
    
    suspend fun createChat(chat: Chat): String {
        chatDao.insertChat(chat)
        return chat.id
    }
    
    suspend fun updateChat(chat: Chat) {
        chatDao.updateChat(chat)
    }
    
    suspend fun deleteChat(chatId: String) {
        chatDao.deleteChat(chatId)
    }
    
    suspend fun getChatMessages(chatId: String): List<Message> {
        return messageDao.getChatMessages(chatId)
    }
    
    fun getChatMessagesFlow(chatId: String): Flow<List<Message>> {
        return messageDao.getChatMessagesFlow(chatId)
    }
    
    suspend fun sendMessage(message: Message) {
        messageDao.insertMessage(message)
        
        // Update chat with last message info
        val chat = chatDao.getChatById(message.chatId)
        chat?.let { existingChat ->
            val updatedChat = existingChat.copy(
                lastMessage = message.content,
                lastMessageTime = message.timestamp,
                lastMessageSenderId = message.senderId
            )
            chatDao.updateChat(updatedChat)
        }
    }
    
    suspend fun markMessagesAsRead(chatId: String, userId: String) {
        messageDao.markMessagesAsRead(chatId, userId)
        
        // Update unread count in chat
        val chat = chatDao.getChatById(chatId)
        chat?.let { existingChat ->
            val unreadCount = messageDao.getUnreadMessageCount(chatId, userId)
            val updatedUnreadMap = existingChat.unreadCount.toMutableMap()
            updatedUnreadMap[userId] = unreadCount
            val updatedChat = existingChat.copy(unreadCount = updatedUnreadMap.toMap())
            chatDao.updateChat(updatedChat)
        }
    }
    
    suspend fun getOrCreateChat(user1Id: String, user2Id: String): Chat {
        // Try to find existing chat
        val existingChat = chatDao.getChatBetweenUsers(user1Id, user2Id)
        if (existingChat != null) {
            return existingChat
        }
        
        // Create new chat
        val newChat = Chat(
            id = generateChatId(user1Id, user2Id),
            participants = listOf(user1Id, user2Id),
            lastMessage = "",
            lastMessageTime = System.currentTimeMillis(),
            lastMessageSenderId = "",
            unreadCount = mapOf(user1Id to 0, user2Id to 0),
            createdAt = System.currentTimeMillis()
        )
        
        chatDao.insertChat(newChat)
        return newChat
    }
    
    private fun generateChatId(user1Id: String, user2Id: String): String {
        // Create consistent chat ID regardless of user order
        val sortedIds = listOf(user1Id, user2Id).sorted()
        return "chat_${sortedIds[0]}_${sortedIds[1]}"
    }
    
    // Real-time Firebase Features
    fun getUserChatsFlow(userId: String): Flow<List<Chat>> = callbackFlow {
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fallback to local data
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    val chats = querySnapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
                    // Cache locally
                    trySend(chats)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    fun getChatMessagesRealTime(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    val messages = querySnapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                    // Cache locally
                    trySend(messages)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun sendMessageFirebase(message: Message): Result<String> {
        return try {
            val messageId = message.id.ifEmpty { UUID.randomUUID().toString() }
            val messageWithId = message.copy(id = messageId)
            
            // Send to Firestore
            firestore.collection("messages").document(messageId).set(messageWithId).await()
            
            // Update chat
            val chatRef = firestore.collection("chats").document(message.chatId)
            val chatSnapshot = chatRef.get().await()
            
            if (chatSnapshot.exists()) {
                val chat = chatSnapshot.toObject(Chat::class.java)
                chat?.let { existingChat ->
                    val updatedChat = existingChat.copy(
                        lastMessage = message.content,
                        lastMessageTime = message.timestamp,
                        lastMessageSenderId = message.senderId
                    )
                    chatRef.set(updatedChat).await()
                }
            } else {
                // Create new chat if it doesn't exist
                val newChat = Chat(
                    id = message.chatId,
                    participants = listOf(message.senderId), // Will be updated when other user joins
                    lastMessage = message.content,
                    lastMessageTime = message.timestamp,
                    lastMessageSenderId = message.senderId,
                    createdAt = System.currentTimeMillis()
                )
                chatRef.set(newChat).await()
            }
            
            // Cache locally
            messageDao.insertMessage(messageWithId)
            
            Result.success(messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendImageMessage(chatId: String, senderId: String, senderName: String, imageUri: android.net.Uri): Result<String> {
        return try {
            val messageId = UUID.randomUUID().toString()
            
            // Upload image
            val imageRef = storage.reference.child("chat_images/$chatId/$messageId.jpg")
            val uploadTask = imageRef.putFile(imageUri).await()
            val imageUrl = uploadTask.storage.downloadUrl.await().toString()
            
            // Create message
            val message = Message(
                id = messageId,
                chatId = chatId,
                senderId = senderId,
                senderName = senderName,
                content = "Image",
                type = MessageType.IMAGE,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            
            sendMessageFirebase(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createOrGetChatFirebase(user1Id: String, user2Id: String, user1Name: String, user2Name: String): Result<String> {
        return try {
            val chatId = generateChatId(user1Id, user2Id)
            val chatRef = firestore.collection("chats").document(chatId)
            val snapshot = chatRef.get().await()
            
            if (!snapshot.exists()) {
                val newChat = Chat(
                    id = chatId,
                    participants = listOf(user1Id, user2Id),
                    lastMessage = "",
                    lastMessageTime = System.currentTimeMillis(),
                    lastMessageSenderId = "",
                    unreadCount = mapOf(user1Id to 0, user2Id to 0),
                    createdAt = System.currentTimeMillis()
                )
                
                chatRef.set(newChat).await()
                chatDao.insertChat(newChat)
            }
            
            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markMessagesAsReadFirebase(chatId: String, userId: String): Result<Unit> {
        return try {
            // Update messages in Firestore
            val messagesSnapshot = firestore.collection("messages")
                .whereEqualTo("chatId", chatId)
                .whereNotEqualTo("senderId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            messagesSnapshot.documents.forEach { doc ->
                doc.reference.update("isRead", true)
            }
            
            // Update chat unread count
            val chatRef = firestore.collection("chats").document(chatId)
            val chatSnapshot = chatRef.get().await()
            val chat = chatSnapshot.toObject(Chat::class.java)
            
            chat?.let { existingChat ->
                val updatedUnreadMap = existingChat.unreadCount.toMutableMap()
                updatedUnreadMap[userId] = 0
                val updatedChat = existingChat.copy(unreadCount = updatedUnreadMap.toMap())
                chatRef.set(updatedChat).await()
            }
            
            // Update local database
            messageDao.markMessagesAsRead(chatId, userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}