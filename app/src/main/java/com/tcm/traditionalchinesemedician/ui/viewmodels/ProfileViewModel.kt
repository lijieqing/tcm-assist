package com.tcm.traditionalchinesemedician.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    // UI state for ProfileScreen
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulate loading user profile
            // This would be replaced with actual user data loading logic
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(500)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        username = "中医爱好者",
                        favoriteHerbsCount = 0,
                        recentViewsCount = 0
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
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val favoriteHerbsCount: Int = 0,
    val recentViewsCount: Int = 0,
    val error: String? = null
) 