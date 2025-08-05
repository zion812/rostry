package com.rio.rostry.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    companion object {
        private val SESSION_KEY = stringPreferencesKey("user_session")
    }

    @Serializable
    data class UserSession(
        val userId: String,
        val email: String,
        val displayName: String,
        val sessionId: String,
        val createdAt: Long = System.currentTimeMillis(),
        val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
        val activeOrganizationId: String? = null,
        val organizationRoleId: String? = null,
        val effectivePermissions: Set<String> = emptySet()
    )

    /**
     * Get current session from DataStore
     */
    fun getCurrentSession(): Flow<UserSession?> {
        return context.dataStore.data.map { preferences ->
            preferences[SESSION_KEY]?.let { sessionJson ->
                try {
                    json.decodeFromString<UserSession>(sessionJson)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    /**
     * Create new session
     */
    suspend fun createSession(
        userId: String,
        email: String,
        displayName: String,
        sessionId: String = generateSessionId()
    ): Result<UserSession> {
        return try {
            val session = UserSession(
                userId = userId,
                email = email,
                displayName = displayName,
                sessionId = sessionId
            )

            // Save to DataStore
            context.dataStore.edit { preferences ->
                preferences[SESSION_KEY] = json.encodeToString(UserSession.serializer(), session)
            }

            _userSession.value = session
            _isAuthenticated.value = true

            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update session with organization context
     */
    suspend fun updateSessionWithOrganization(
        organizationId: String,
        roleId: String,
        permissions: Set<String>
    ): Result<UserSession> {
        return try {
            val currentSession = _userSession.value
                ?: return Result.failure(Exception("No active session"))

            val updatedSession = currentSession.copy(
                activeOrganizationId = organizationId,
                organizationRoleId = roleId,
                effectivePermissions = permissions
            )

            // Save to DataStore
            context.dataStore.edit { preferences ->
                preferences[SESSION_KEY] = json.encodeToString(UserSession.serializer(), updatedSession)
            }

            _userSession.value = updatedSession

            Result.success(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Switch organization context
     */
    suspend fun switchOrganization(organizationId: String): Result<Unit> {
        return try {
            val currentSession = _userSession.value
                ?: return Result.failure(Exception("No active session"))

            // For now, just update the organization ID
            // In a full implementation, this would fetch the user's role and permissions for the new organization
            val updatedSession = currentSession.copy(
                activeOrganizationId = organizationId,
                organizationRoleId = "member", // Default role
                effectivePermissions = emptySet() // Would be fetched from organization membership
            )

            context.dataStore.edit { preferences ->
                preferences[SESSION_KEY] = json.encodeToString(UserSession.serializer(), updatedSession)
            }

            _userSession.value = updatedSession

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if user has specific permission
     */
    fun hasPermission(permission: String): Boolean {
        return _userSession.value?.effectivePermissions?.contains(permission) ?: false
    }

    /**
     * Validate current session
     */
    suspend fun validateSession(): Boolean {
        val session = _userSession.value ?: return false
        
        // Check if session is expired
        if (System.currentTimeMillis() > session.expiresAt) {
            clearSession()
            return false
        }

        // In a full implementation, this would verify with the server
        return true
    }

    /**
     * Refresh session
     */
    suspend fun refreshSession(): Result<UserSession> {
        return try {
            val currentSession = _userSession.value
                ?: return Result.failure(Exception("No active session"))

            // In a full implementation, this would refresh the session with the server
            val refreshedSession = currentSession.copy(
                expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // Extend by 24 hours
            )

            context.dataStore.edit { preferences ->
                preferences[SESSION_KEY] = json.encodeToString(UserSession.serializer(), refreshedSession)
            }

            _userSession.value = refreshedSession

            Result.success(refreshedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear session
     */
    suspend fun clearSession() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(SESSION_KEY)
            }
            _userSession.value = null
            _isAuthenticated.value = false
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }

    /**
     * Initialize session manager
     */
    suspend fun initialize() {
        getCurrentSession().collect { session ->
            _userSession.value = session
            _isAuthenticated.value = session != null && validateSession()
        }
    }

    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}