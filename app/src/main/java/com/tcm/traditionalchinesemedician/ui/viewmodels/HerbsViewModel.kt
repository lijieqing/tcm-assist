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

class HerbsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = HerbRepository.getInstance(application)
    
    // UI state for HerbsScreen
    private val _uiState = MutableStateFlow(HerbsUiState())
    val uiState: StateFlow<HerbsUiState> = _uiState.asStateFlow()
    
    init {
        loadAllHerbs()
        loadCategories()
    }
    
    fun loadAllHerbs(page: Int = 0, pageSize: Int = 20) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val herbs = repository.getAllHerbsPaged(page, pageSize)
                
                _uiState.update { currentState ->
                    val updatedHerbs = if (page == 0) {
                        herbs
                    } else {
                        currentState.herbs + herbs
                    }
                    
                    currentState.copy(
                        isLoading = false,
                        herbs = updatedHerbs,
                        currentPage = page,
                        hasMoreData = herbs.size == pageSize
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
    
    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = repository.getAllCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
    
    fun loadHerbsByCategory(category: String, page: Int = 0, pageSize: Int = 20) {
        // 检查是否已经是当前选中的分类，且是第一页
        if (category == _uiState.value.selectedCategory && page == 0 && _uiState.value.herbs.isNotEmpty()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedCategory = category, searchQuery = "") }
            
            try {
                val herbs = repository.getHerbsByCategoryPaged(category, pageSize, page * pageSize)
                
                _uiState.update { currentState ->
                    val updatedHerbs = if (page == 0) {
                        herbs
                    } else {
                        currentState.herbs + herbs
                    }
                    
                    currentState.copy(
                        isLoading = false,
                        herbs = updatedHerbs,
                        currentPage = page,
                        hasMoreData = herbs.size == pageSize
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
    
    fun searchHerbs(query: String, page: Int = 0, pageSize: Int = 20) {
        viewModelScope.launch {
            // 保留当前选中的分类，不再强制设置为null
            _uiState.update { it.copy(isLoading = true, error = null, searchQuery = query) }
            
            try {
                // 使用当前选中分类和搜索词进行搜索
                val herbs = if (_uiState.value.selectedCategory != null && _uiState.value.selectedCategory != "全部") {
                    // 如果有选中的分类，在该分类内搜索
                    repository.searchHerbsInCategory(_uiState.value.selectedCategory!!, query, pageSize, page * pageSize)
                } else {
                    // 否则，在所有中药中搜索
                    repository.searchHerbsPaged(query, pageSize, page * pageSize)
                }
                
                _uiState.update { currentState ->
                    val updatedHerbs = if (page == 0) {
                        herbs
                    } else {
                        currentState.herbs + herbs
                    }
                    
                    currentState.copy(
                        isLoading = false,
                        herbs = updatedHerbs,
                        currentPage = page,
                        hasMoreData = herbs.size == pageSize
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
    
    fun resetSearch() {
        // 保存当前搜索状态，以便判断是否需要重新加载
        val currentSearchQuery = _uiState.value.searchQuery
        val currentSelectedCategory = _uiState.value.selectedCategory
        
        // 清除搜索内容，但保留当前选中的分类
        _uiState.update { it.copy(searchQuery = "") }
        
        // 根据当前状态决定加载的数据
        if (currentSearchQuery.isNotBlank()) {
            // 如果之前有搜索内容，根据当前选中的分类加载数据
            if (currentSelectedCategory != null) {
                // 如果有选中的分类，加载该分类的数据
                loadHerbsByCategory(currentSelectedCategory)
            } else {
                // 否则加载全部数据
                loadAllHerbs()
            }
        }
    }
    
    /**
     * 仅更新选中的分类，不清除搜索内容，不加载数据
     * 用于搜索状态下的Tab切换
     */
    fun updateSelectedCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
    
    /**
     * 完全重置所有状态，包括搜索内容和选中的分类，并加载全部数据
     */
    fun resetToAll() {
        _uiState.update { it.copy(searchQuery = "", selectedCategory = null) }
        loadAllHerbs()
    }
    
    /**
     * 只更新搜索查询内容，不触发搜索操作
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    /**
     * 执行搜索操作，使用当前的查询内容和选中的分类
     */
    fun performSearch() {
        val currentState = _uiState.value
        loadDataWithCurrentState(0) // 从第一页开始搜索
    }
    
    /**
     * 使用当前UI状态加载数据
     * @param page 要加载的页码，默认为0（第一页）
     */
    fun loadDataWithCurrentState(page: Int = 0) {
        val currentState = _uiState.value
        val query = currentState.searchQuery
        val category = currentState.selectedCategory
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val herbs = when {
                    // 有搜索内容且有选中分类
                    query.isNotBlank() && category != null -> {
                        repository.searchHerbsInCategory(category, query, ITEMS_PER_PAGE, page * ITEMS_PER_PAGE)
                    }
                    // 只有搜索内容
                    query.isNotBlank() -> {
                        repository.searchHerbsPaged(query, ITEMS_PER_PAGE, page * ITEMS_PER_PAGE)
                    }
                    // 只有选中分类
                    category != null -> {
                        repository.getHerbsByCategoryPaged(category, ITEMS_PER_PAGE, page * ITEMS_PER_PAGE)
                    }
                    // 没有搜索内容和选中分类，加载全部
                    else -> {
                        repository.getAllHerbsPaged(page, ITEMS_PER_PAGE)
                    }
                }
                
                _uiState.update { state ->
                    val updatedHerbs = if (page == 0) {
                        herbs
                    } else {
                        state.herbs + herbs
                    }
                    
                    state.copy(
                        isLoading = false,
                        herbs = updatedHerbs,
                        currentPage = page,
                        hasMoreData = herbs.size == ITEMS_PER_PAGE
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
    
    /**
     * 加载更多数据，使用当前状态
     */
    fun loadMoreHerbs() {
        val nextPage = _uiState.value.currentPage + 1
        loadDataWithCurrentState(nextPage)
    }
    
    // 常量定义
    companion object {
        private const val ITEMS_PER_PAGE = 20
    }
}

data class HerbsUiState(
    val isLoading: Boolean = false,
    val herbs: List<Herb> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val currentPage: Int = 0,
    val hasMoreData: Boolean = true,
    val error: String? = null
) 