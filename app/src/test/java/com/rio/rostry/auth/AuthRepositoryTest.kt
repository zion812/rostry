package com.rio.rostry.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.rio.rostry.data.local.dao.UserDao
import com.rio.rostry.data.model.User
import com.rio.rostry.data.model.UserRole
import com.rio.rostry.data.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUserDao: UserDao
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var mockAuthResult: AuthResult
    private lateinit var mockCollectionReference: CollectionReference
    private lateinit var mockDocumentReference: DocumentReference
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    @Before
    fun setup() {
        mockFirebaseAuth = mockk()
        mockFirestore = mockk()
        mockUserDao = mockk(relaxed = true)
        mockFirebaseUser = mockk()
        mockAuthResult = mockk()
        mockCollectionReference = mockk()
        mockDocumentReference = mockk()
        mockDocumentSnapshot = mockk()

        authRepository = AuthRepository(mockFirebaseAuth, mockFirestore, mockUserDao)
    }

    @Test
    fun `should successfully sign in user with valid email and password`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"
        
        every { mockFirebaseUser.uid } returns userId
        every { mockAuthResult.user } returns mockFirebaseUser
        every { mockFirebaseAuth.signInWithEmailAndPassword(email, password) } returns 
            Tasks.forResult(mockAuthResult)
        
        // Mock Firestore operations for user sync
        every { mockFirestore.collection("users") } returns mockCollectionReference
        every { mockCollectionReference.document(userId) } returns mockDocumentReference
        every { mockDocumentReference.get() } returns Tasks.forResult(mockDocumentSnapshot)
        every { mockDocumentSnapshot.toObject(User::class.java) } returns User(
            id = userId,
            email = email,
            displayName = "Test User"
        )
        
        coEvery { mockUserDao.insertUser(any()) } just Runs

        // When
        val result = authRepository.signInWithEmail(email, password)

        // Then
        assertTrue("Sign in should succeed", result.isSuccess)
        assertEquals("Should return correct user", mockFirebaseUser, result.getOrNull())
        
        verify { mockFirebaseAuth.signInWithEmailAndPassword(email, password) }
        coVerify { mockUserDao.insertUser(any()) }
    }

    @Test
    fun `should return failure result when sign in with invalid credentials`() = runTest {
        // Given
        val email = "invalid@example.com"
        val password = "wrongpassword"
        val exception = Exception("Invalid credentials")
        
        every { mockFirebaseAuth.signInWithEmailAndPassword(email, password) } returns 
            Tasks.forException(exception)

        // When
        val result = authRepository.signInWithEmail(email, password)

        // Then
        assertTrue("Sign in should fail", result.isFailure)
        assertEquals("Should return correct exception", exception, result.exceptionOrNull())
        
        verify { mockFirebaseAuth.signInWithEmailAndPassword(email, password) }
        coVerify(exactly = 0) { mockUserDao.insertUser(any()) }
    }

    @Test
    fun `should create new user account and sync to local database`() = runTest {
        // Given
        val email = "newuser@example.com"
        val password = "password123"
        val displayName = "New User"
        val role = UserRole.FARMER
        val userId = "newuser123"
        
        every { mockFirebaseUser.uid } returns userId
        every { mockAuthResult.user } returns mockFirebaseUser
        every { mockFirebaseAuth.createUserWithEmailAndPassword(email, password) } returns 
            Tasks.forResult(mockAuthResult)
        
        // Mock Firestore operations
        every { mockFirestore.collection("users") } returns mockCollectionReference
        every { mockCollectionReference.document(userId) } returns mockDocumentReference
        every { mockDocumentReference.set(any()) } returns Tasks.forResult(null)
        every { mockDocumentReference.get() } returns Tasks.forResult(mockDocumentSnapshot)
        every { mockDocumentSnapshot.toObject(User::class.java) } returns User(
            id = userId,
            email = email,
            displayName = displayName,
            role = role
        )
        
        coEvery { mockUserDao.insertUser(any()) } just Runs

        // When
        val result = authRepository.signUpWithEmail(email, password, displayName, role)

        // Then
        assertTrue("Sign up should succeed", result.isSuccess)
        assertEquals("Should return correct user", mockFirebaseUser, result.getOrNull())
        
        verify { mockFirebaseAuth.createUserWithEmailAndPassword(email, password) }
        verify { mockDocumentReference.set(any()) }
        coVerify { mockUserDao.insertUser(any()) }
    }

    @Test
    fun `should handle network failures gracefully during authentication`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val networkException = Exception("Network error")
        
        every { mockFirebaseAuth.signInWithEmailAndPassword(email, password) } returns 
            Tasks.forException(networkException)

        // When
        val result = authRepository.signInWithEmail(email, password)

        // Then
        assertTrue("Should handle network failure", result.isFailure)
        assertTrue("Should contain network error", 
            result.exceptionOrNull()?.message?.contains("Network error") == true)
        
        verify { mockFirebaseAuth.signInWithEmailAndPassword(email, password) }
    }

    @Test
    fun `should sign out user and clear session properly`() = runTest {
        // Given
        every { mockFirebaseAuth.signOut() } just Runs

        // When
        authRepository.signOut()

        // Then
        verify { mockFirebaseAuth.signOut() }
    }

    @Test
    fun `should return current user when authenticated`() = runTest {
        // Given
        val userId = "user123"
        val user = User(
            id = userId,
            email = "test@example.com",
            displayName = "Test User"
        )
        
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns userId
        coEvery { mockUserDao.getUserById(userId) } returns user

        // When
        val result = authRepository.getCurrentUser()

        // Then
        assertEquals("Should return current user", user, result)
        coVerify { mockUserDao.getUserById(userId) }
    }

    @Test
    fun `should return null when no user is authenticated`() = runTest {
        // Given
        every { mockFirebaseAuth.currentUser } returns null

        // When
        val result = authRepository.getCurrentUser()

        // Then
        assertNull("Should return null when not authenticated", result)
    }

    @Test
    fun `should send password reset email successfully`() = runTest {
        // Given
        val email = "test@example.com"
        every { mockFirebaseAuth.sendPasswordResetEmail(email) } returns Tasks.forResult(null)

        // When
        val result = authRepository.resetPassword(email)

        // Then
        assertTrue("Password reset should succeed", result.isSuccess)
        verify { mockFirebaseAuth.sendPasswordResetEmail(email) }
    }

    @Test
    fun `should handle password reset failure`() = runTest {
        // Given
        val email = "invalid@example.com"
        val exception = Exception("User not found")
        every { mockFirebaseAuth.sendPasswordResetEmail(email) } returns Tasks.forException(exception)

        // When
        val result = authRepository.resetPassword(email)

        // Then
        assertTrue("Password reset should fail", result.isFailure)
        assertEquals("Should return correct exception", exception, result.exceptionOrNull())
        verify { mockFirebaseAuth.sendPasswordResetEmail(email) }
    }

    @Test
    fun `should check user login status correctly`() {
        // Given - user is logged in
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // When
        val isLoggedIn = authRepository.isUserLoggedIn()

        // Then
        assertTrue("Should return true when user is logged in", isLoggedIn)

        // Given - user is not logged in
        every { mockFirebaseAuth.currentUser } returns null

        // When
        val isNotLoggedIn = authRepository.isUserLoggedIn()

        // Then
        assertFalse("Should return false when user is not logged in", isNotLoggedIn)
    }
}