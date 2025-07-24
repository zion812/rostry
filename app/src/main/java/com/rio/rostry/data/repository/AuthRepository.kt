package com.rio.rostry.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.data.local.dao.UserDao
import com.rio.rostry.data.model.User
import com.rio.rostry.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) {
    
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser
    
    fun isUserLoggedIn(): Boolean = currentUser != null
    
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                syncUserToLocal(user)
                Result.success(user)
            } ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signUpWithEmail(
        email: String, 
        password: String, 
        displayName: String,
        role: UserRole
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    role = role
                )
                saveUserToFirestore(user)
                syncUserToLocal(firebaseUser)
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Sign up failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let { user ->
                syncUserToLocal(user)
                Result.success(user)
            } ?: Result.failure(Exception("Google sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }
    
    suspend fun getCurrentUser(): User? {
        return currentUser?.let { firebaseUser ->
            try {
                userDao.getUserById(firebaseUser.uid)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    suspend fun signOut() {
        firebaseAuth.signOut()
    }
    
    suspend fun getCurrentUserProfile(): Flow<User?> = flow {
        currentUser?.let { firebaseUser ->
            try {
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)
                user?.let { 
                    userDao.insertUser(it)
                    emit(it)
                } ?: emit(null)
            } catch (e: Exception) {
                // Fallback to local data
                emit(userDao.getUserById(firebaseUser.uid))
            }
        } ?: emit(null)
    }
    
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id).set(user).await()
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    private suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection("users").document(user.id).set(user).await()
            userDao.insertUser(user)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    private suspend fun syncUserToLocal(firebaseUser: FirebaseUser) {
        try {
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java)
            user?.let { userDao.insertUser(it) }
        } catch (e: Exception) {
            // Create basic user if Firestore fails
            val basicUser = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: ""
            )
            userDao.insertUser(basicUser)
        }
    }
}