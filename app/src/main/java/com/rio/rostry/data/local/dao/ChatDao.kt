package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    
    @Query("SELECT * FROM chats ORDER BY lastMessageTime DESC")
    fun getAllChats(): Flow<List<Chat>>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): Chat?
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun getChatByIdFlow(chatId: String): Flow<Chat?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)
    
    @Update
    suspend fun updateChat(chat: Chat)
    
    @Delete
    suspend fun deleteChat(chat: Chat)
    
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)
    
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)
    
    @Query("SELECT * FROM chats WHERE participants LIKE '%' || :userId || '%' ORDER BY lastMessageTime DESC")
    suspend fun getUserChats(userId: String): List<Chat>
    
    @Query("SELECT * FROM chats WHERE (participants LIKE '%' || :user1Id || '%' AND participants LIKE '%' || :user2Id || '%') LIMIT 1")
    suspend fun getChatBetweenUsers(user1Id: String, user2Id: String): Chat?
}