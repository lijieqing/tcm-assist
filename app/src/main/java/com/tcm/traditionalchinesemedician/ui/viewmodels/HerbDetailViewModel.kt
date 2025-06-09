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

class HerbDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = HerbRepository.getInstance(application)
    
    // UI state for HerbDetailScreen
    private val _uiState = MutableStateFlow(HerbDetailUiState())
    val uiState: StateFlow<HerbDetailUiState> = _uiState.asStateFlow()
    
    fun loadHerbDetail(herbId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val herb = repository.getHerbById(herbId)
                
                if (herb != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            herb = herb
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Herb not found"
                        )
                    }
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
    
    fun retryLoading(herbId: Int) {
        loadHerbDetail(herbId)
    }
}

data class HerbDetailUiState(
    val isLoading: Boolean = false,
    val herb: Herb? = null,
    val error: String? = null
) 