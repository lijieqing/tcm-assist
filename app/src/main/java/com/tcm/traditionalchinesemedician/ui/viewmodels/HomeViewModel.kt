package com.tcm.traditionalchinesemedician.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tcm.traditionalchinesemedician.data.Herb
import com.tcm.traditionalchinesemedician.data.HerbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = HerbRepository.getInstance(application)
    
    // UI state for HomeScreen
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load featured herbs
                val featuredHerbs = repository.getAllHerbsSync().take(3)
                
                // Load categories
                val categories = repository.getAllCategories()
                
                // Load recommended functions and clinical applications
                val recommendedFunctions = repository.getRecommendedFunctions(8)
                val recommendedClinicalApplications = repository.getRecommendedClinicalApplications(8)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        featuredHerbs = featuredHerbs,
                        categories = categories,
                        recommendedFunctions = recommendedFunctions,
                        recommendedClinicalApplications = recommendedClinicalApplications
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
    
    fun retryLoading() {
        loadHomeData()
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val featuredHerbs: List<Herb> = emptyList(),
    val categories: List<String> = emptyList(),
    val recommendedFunctions: List<String> = emptyList(),
    val recommendedClinicalApplications: List<String> = emptyList(),
    val error: String? = null
) 