package com.rio.rostry.ui.fowls

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlRecord
import com.rio.rostry.data.model.FowlType
import com.rio.rostry.data.model.FowlGender
import com.rio.rostry.data.repository.FowlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FowlManagementViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FowlManagementUiState())
    val uiState: StateFlow<FowlManagementUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateBreed(breed: String) {
        _uiState.value = _uiState.value.copy(breed = breed)
    }

    fun updateType(type: FowlType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun updateGender(gender: FowlGender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateWeight(weight: String) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    fun updateColor(color: String) {
        _uiState.value = _uiState.value.copy(color = color)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }

    fun updateMotherId(motherId: String) {
        _uiState.value = _uiState.value.copy(motherId = motherId)
    }

    fun updateFatherId(fatherId: String) {
        _uiState.value = _uiState.value.copy(fatherId = fatherId)
    }

    fun updateInitialCount(count: String) {
        _uiState.value = _uiState.value.copy(initialCount = count)
    }

    fun updateStatus(status: String) {
        _uiState.value = _uiState.value.copy(status = status)
    }

    fun updateRecordType(recordType: String) {
        _uiState.value = _uiState.value.copy(recordType = recordType)
    }

    fun updateRecordDetails(details: String) {
        _uiState.value = _uiState.value.copy(recordDetails = details)
    }

    fun updateSelectedImageUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun updateDateOfHatching(date: Long) {
        _uiState.value = _uiState.value.copy(dateOfHatching = date)
    }

    // Function to add a new fowl with an initial record and proof image
    fun addFowl(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val currentUser = auth.currentUser

        if (currentUser == null) {
            _uiState.value = currentState.copy(error = "User not authenticated")
            return
        }

        if (currentState.name.isBlank() || currentState.breed.isBlank()) {
            _uiState.value = currentState.copy(error = "Name and breed are required")
            return
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val fowlId = UUID.randomUUID().toString()
                
                // Upload proof image if selected
                var proofImageUrl: String? = null
                currentState.selectedImageUri?.let { uri ->
                    val uploadResult = fowlRepository.uploadProofImage(uri, fowlId)
                    if (uploadResult.isSuccess) {
                        proofImageUrl = uploadResult.getOrNull()
                    } else {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            error = "Failed to upload image: ${uploadResult.exceptionOrNull()?.message}"
                        )
                        return@launch
                    }
                }

                // Create fowl object
                val fowl = Fowl(
                    id = fowlId,
                    ownerId = currentUser.uid,
                    name = currentState.name,
                    breed = currentState.breed,
                    type = currentState.type,
                    gender = currentState.gender,
                    weight = currentState.weight.toDoubleOrNull() ?: 0.0,
                    color = currentState.color,
                    description = currentState.description,
                    location = currentState.location,
                    motherId = currentState.motherId.takeIf { it.isNotBlank() },
                    fatherId = currentState.fatherId.takeIf { it.isNotBlank() },
                    dateOfHatching = currentState.dateOfHatching,
                    initialCount = currentState.initialCount.toIntOrNull(),
                    status = currentState.status,
                    proofImageUrl = proofImageUrl
                )

                // Create initial record
                val initialRecord = FowlRecord(
                    recordId = UUID.randomUUID().toString(),
                    fowlId = fowlId,
                    recordType = currentState.recordType,
                    date = System.currentTimeMillis(),
                    details = currentState.recordDetails,
                    proofImageUrl = proofImageUrl,
                    weight = currentState.weight.toDoubleOrNull(),
                    createdBy = currentUser.uid
                )

                // Save fowl and record
                val result = fowlRepository.addFowlWithRecord(fowl, initialRecord)
                
                if (result.isSuccess) {
                    _uiState.value = FowlManagementUiState() // Reset form
                    onSuccess()
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = "Failed to add fowl: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class FowlManagementUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val breed: String = "",
    val type: FowlType = FowlType.CHICKEN,
    val gender: FowlGender = FowlGender.UNKNOWN,
    val weight: String = "",
    val color: String = "",
    val description: String = "",
    val location: String = "",
    val motherId: String = "",
    val fatherId: String = "",
    val initialCount: String = "",
    val status: String = "Growing",
    val dateOfHatching: Long = System.currentTimeMillis(),
    val recordType: String = "Initial Record",
    val recordDetails: String = "",
    val selectedImageUri: Uri? = null,
    val error: String? = null
)