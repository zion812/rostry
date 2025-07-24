package com.rio.rostry.ui.fowls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                imageUrls = emptyList() // TODO: Add image upload functionality
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
    val error: String? = null
)