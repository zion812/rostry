package com.rio.rostry.data.model

data class ChatUiModel(
    val id: String,
    val otherUserName: String,
    val otherUserImageUrl: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int
)

fun Chat.toUiModel(currentUserId: String, otherUserName: String, otherUserImageUrl: String = ""): ChatUiModel {
    return ChatUiModel(
        id = id,
        otherUserName = otherUserName,
        otherUserImageUrl = otherUserImageUrl,
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime,
        unreadCount = unreadCount[currentUserId] ?: 0
    )
}