package com.rio.rostry.ui.fowls

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlGender
import com.rio.rostry.data.model.FowlType
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.FowlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddFowlViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddFowlUiState())
    val uiState: StateFlow<AddFowlUiState> = _uiState.asStateFlow()
    
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
    
    fun updatePrice(price: String) {
        _uiState.value = _uiState.value.copy(price = price)
    }
    
    fun updateIsForSale(isForSale: Boolean) {
        _uiState.value = _uiState.value.copy(isForSale = isForSale)
    }
    
    fun addImage(imageUri: Uri) {
        val currentImages = _uiState.value.selectedImages.toMutableList()
        if (currentImages.size < 5) { // Limit to 5 images
            currentImages.add(imageUri)
            _uiState.value = _uiState.value.copy(selectedImages = currentImages)
        }
    }
    
    fun removeImage(imageUri: Uri) {
        val currentImages = _uiState.value.selectedImages.toMutableList()
        currentImages.remove(imageUri)
        _uiState.value = _uiState.value.copy(selectedImages = currentImages)
    }
    
    private suspend fun uploadImages(images: List<Uri>): List<String> {
        val storage = FirebaseStorage.getInstance()
        val uploadedUrls = mutableListOf<String>()
        
        for (imageUri in images) {
            try {
                val fileName = "fowl_images/${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(fileName)
                
                val uploadTask = storageRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                uploadedUrls.add(downloadUrl.toString())
            } catch (e: Exception) {
                // Log error but continue with other images
                continue
            }
        }
        
        return uploadedUrls
    }
    
    fun addFowl(onSuccess: () -> Unit) {
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }
        
        val state = _uiState.value
        
        // Validation
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Name is required")
            return
        }
        
        if (state.breed.isBlank()) {
            _uiState.value = state.copy(error = "Breed is required")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            try {
                // Upload images first if any are selected
                val imageUrls = if (state.selectedImages.isNotEmpty()) {
                    uploadImages(state.selectedImages)
                } else {
                    emptyList()
                }
                
                val fowl = Fowl(
                    ownerId = currentUser.uid,
                    name = state.name.trim(),
                    breed = state.breed.trim(),
                    type = state.type,
                    gender = state.gender,
                    weight = state.weight.toDoubleOrNull() ?: 0.0,
                    color = state.color.trim(),
                    description = state.description.trim(),
                    location = state.location.trim(),
                    isForSale = state.isForSale,
                    price = if (state.isForSale) state.price.toDoubleOrNull() ?: 0.0 else 0.0,
                    imageUrls = imageUrls
                )
                
                fowlRepository.addFowl(fowl)
                    .onSuccess {
                        _uiState.value = state.copy(isLoading = false)
                        onSuccess()
                    }
                    .onFailure { error ->
                        _uiState.value = state.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to add fowl"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to upload images"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AddFowlUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val breed: String = "",
    val type: FowlType = FowlType.CHICKEN,
    val gender: FowlGender = FowlGender.UNKNOWN,
    val weight: String = "",
    val color: String = "",
    val description: String = "",
    val location: String = "",
    val isForSale: Boolean = false,
    val price: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val error: String? = null
)