package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.data.local.dao.UserDao
import com.rio.rostry.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {
    
    fun getUserFlow(userId: String): Flow<User?> {
        return userDao.getUserFlow(userId)
    }
    
    suspend fun getUser(userId: String): User? {
        return try {
            // Try to get from local database first
            val localUser = userDao.getUser(userId)
            if (localUser != null) {
                return localUser
            }
            
            // If not found locally, fetch from Firestore
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                user?.let {
                    // Cache in local database
                    userDao.insertUser(it)
                }
                user
            } else {
                null
            }
        } catch (e: Exception) {
            // If Firestore fails, try local database
            userDao.getUser(userId)
        }
    }
    
    suspend fun insertUser(user: User) {
        try {
            // Insert into local database
            userDao.insertUser(user)
            
            // Sync to Firestore
            firestore.collection("users").document(user.id).set(user).await()
        } catch (e: Exception) {
            // If Firestore fails, at least save locally
            userDao.insertUser(user)
            throw e
        }
    }
    
    suspend fun updateUser(user: User) {
        try {
            // Update local database
            userDao.updateUser(user)
            
            // Sync to Firestore
            firestore.collection("users").document(user.id).set(user).await()
        } catch (e: Exception) {
            // If Firestore fails, at least update locally
            userDao.updateUser(user)
            throw e
        }
    }
    
    suspend fun deleteUser(user: User) {
        try {
            // Delete from local database
            userDao.deleteUser(user)
            
            // Delete from Firestore
            firestore.collection("users").document(user.id).delete().await()
        } catch (e: Exception) {
            // If Firestore fails, at least delete locally
            userDao.deleteUser(user)
            throw e
        }
    }
    
    suspend fun searchUsers(query: String): List<User> {
        return try {
            // Search in Firestore
            val documents = firestore.collection("users")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get()
                .await()
            
            val users = documents.toObjects(User::class.java)
            
            // Cache results locally
            users.forEach { user ->
                userDao.insertUser(user)
            }
            
            users
        } catch (e: Exception) {
            // If Firestore fails, search locally
            userDao.searchUsers("%$query%")
        }
    }
    
    suspend fun getAllUsersList(): List<User> {
        return userDao.getAllUsers()
    }
    
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        try {
            val user = getUser(userId)
            user?.let {
                val updatedUser = it.copy(
                    isOnline = isOnline,
                    lastSeen = if (!isOnline) System.currentTimeMillis() else it.lastSeen
                )
                updateUser(updatedUser)
            }
        } catch (e: Exception) {
            // Handle error silently for online status updates
        }
    }
    
    suspend fun getUsersByIds(userIds: List<String>): List<User> {
        return try {
            if (userIds.isEmpty()) return emptyList()
            
            // Try to get from local database first
            val localUsers = userDao.getUsersByIds(userIds)
            val localUserIds = localUsers.map { it.id }.toSet()
            val missingIds = userIds.filter { it !in localUserIds }
            
            if (missingIds.isEmpty()) {
                return localUsers
            }
            
            // Fetch missing users from Firestore
            val chunks = missingIds.chunked(10) // Firestore 'in' query limit is 10
            val remoteUsers = mutableListOf<User>()
            
            chunks.forEach { chunk ->
                val documents = firestore.collection("users")
                    .whereIn("id", chunk)
                    .get()
                    .await()
                
                val users = documents.toObjects(User::class.java)
                remoteUsers.addAll(users)
                
                // Cache in local database
                users.forEach { user ->
                    userDao.insertUser(user)
                }
            }
            
            localUsers + remoteUsers
        } catch (e: Exception) {
            // If Firestore fails, return what we have locally
            userDao.getUsersByIds(userIds)
        }
    }
    
    suspend fun getRecentUsers(limit: Int): List<User> {
        return userDao.getRecentUsers(limit)
    }
    
    suspend fun getUserById(userId: String): User? {
        return getUser(userId)
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return try {
            // Try to get from local database first
            val localUser = userDao.getUserByEmail(email)
            if (localUser != null) {
                return localUser
            }
            
            // If not found locally, fetch from Firestore
            val documents = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            if (!documents.isEmpty) {
                val user = documents.documents[0].toObject(User::class.java)
                user?.let {
                    // Cache in local database
                    userDao.insertUser(it)
                }
                user
            } else {
                null
            }
        } catch (e: Exception) {
            // If Firestore fails, try local database
            userDao.getUserByEmail(email)
        }
    }
}