package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlRepository: FowlRepository
) {
    
    suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: String = "general"
    ) {
        try {
            val notification = mapOf(
                "userId" to userId,
                "title" to title,
                "message" to message,
                "type" to type,
                "timestamp" to System.currentTimeMillis(),
                "read" to false
            )
            
            firestore.collection("notifications")
                .add(notification)
                .await()
        } catch (e: Exception) {
            // Handle error silently for notifications
        }
    }
    
    suspend fun getNotifications(userId: String): List<Map<String, Any>> {
        return try {
            val documents = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            documents.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun markAsRead(notificationId: String) {
        try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}