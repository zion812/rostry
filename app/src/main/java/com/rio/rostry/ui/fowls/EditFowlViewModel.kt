package com.rio.rostry.ui.fowls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlGender
import com.rio.rostry.data.model.FowlType
import com.rio.rostry.data.repository.FowlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditFowlUiState(
    val isLoading: Boolean = false,
    val fowl: Fowl? = null,
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

@HiltViewModel
class EditFowlViewModel @Inject constructor(
    private val fowlRepository: FowlRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditFowlUiState())
    val uiState: StateFlow<EditFowlUiState> = _uiState.asStateFlow()
    
    fun loadFowl(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val fowl = fowlRepository.getFowlById(fowlId)
                if (fowl != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        fowl = fowl,
                        name = fowl.name,
                        breed = fowl.breed,
                        type = fowl.type,
                        gender = fowl.gender,
                        weight = if (fowl.weight > 0) fowl.weight.toString() else "",
                        color = fowl.color,
                        description = fowl.description,
                        location = fowl.location,
                        isForSale = fowl.isForSale,
                        price = if (fowl.price > 0) fowl.price.toString() else ""
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Fowl not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load fowl: ${e.message}"
                )
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }
    
    fun updateBreed(breed: String) {
        _uiState.value = _uiState.value.copy(breed = breed, error = null)
    }
    
    fun updateType(type: FowlType) {
        _uiState.value = _uiState.value.copy(type = type, error = null)
    }
    
    fun updateGender(gender: FowlGender) {
        _uiState.value = _uiState.value.copy(gender = gender, error = null)
    }
    
    fun updateWeight(weight: String) {
        _uiState.value = _uiState.value.copy(weight = weight, error = null)
    }
    
    fun updateColor(color: String) {
        _uiState.value = _uiState.value.copy(color = color, error = null)
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description, error = null)
    }
    
    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location, error = null)
    }
    
    fun updateIsForSale(isForSale: Boolean) {
        _uiState.value = _uiState.value.copy(
            isForSale = isForSale,
            price = if (!isForSale) "" else _uiState.value.price,
            error = null
        )
    }
    
    fun updatePrice(price: String) {
        _uiState.value = _uiState.value.copy(price = price, error = null)
    }
    
    fun updateFowl(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        if (currentState.name.isBlank()) {
            _uiState.value = currentState.copy(error = "Name is required")
            return
        }
        
        if (currentState.breed.isBlank()) {
            _uiState.value = currentState.copy(error = "Breed is required")
            return
        }
        
        val fowl = currentState.fowl ?: return
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                val weight = currentState.weight.toDoubleOrNull() ?: 0.0
                val price = if (currentState.isForSale) {
                    currentState.price.toDoubleOrNull() ?: 0.0
                } else {
                    0.0
                }
                
                val updatedFowl = fowl.copy(
                    name = currentState.name.trim(),
                    breed = currentState.breed.trim(),
                    type = currentState.type,
                    gender = currentState.gender,
                    weight = weight,
                    color = currentState.color.trim(),
                    description = currentState.description.trim(),
                    location = currentState.location.trim(),
                    isForSale = currentState.isForSale,
                    price = price,
                    updatedAt = System.currentTimeMillis()
                )
                
                fowlRepository.updateFowl(updatedFowl)
                
                _uiState.value = currentState.copy(isLoading = false)
                onSuccess()
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "Failed to update fowl: ${e.message}"
                )
            }
        }
    }
    
    fun deleteFowl(onSuccess: () -> Unit) {
        val fowl = _uiState.value.fowl ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                fowlRepository.deleteFowl(fowl.id)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete fowl: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}