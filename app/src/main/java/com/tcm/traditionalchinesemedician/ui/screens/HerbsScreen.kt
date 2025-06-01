package com.tcm.traditionalchinesemedician.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tcm.traditionalchinesemedician.R
import com.tcm.traditionalchinesemedician.data.Herb
import com.tcm.traditionalchinesemedician.data.HerbRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HerbsScreen(
    selectedCategory: String? = null,
    onHerbClick: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allCategories = listOf("全部") + HerbRepository.categories
    
    // 计算初始页面索引
    val initialPage = if (selectedCategory == "null" || selectedCategory == null) 0 
        else allCategories.indexOf(selectedCategory).coerceAtLeast(0)
    
    // Pager state for horizontal swiping
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { allCategories.size }
    )
    
    // Current category based on pager position
    val currentCategory = allCategories[pagerState.currentPage]
    
    val coroutineScope = rememberCoroutineScope()
    
    // 获取搜索结果
    val searchResults = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            emptyList()
        } else {
            HerbRepository.searchHerbs(searchQuery)
        }
    }
    
    // 根据当前分类和搜索结果过滤药材
    val filteredHerbs = remember(searchQuery, currentCategory, searchResults) {
        if (searchQuery.isEmpty()) {
            // 没有搜索时，根据分类显示
            if (currentCategory == "全部") {
                HerbRepository.getAllHerbs()
            } else {
                HerbRepository.getHerbsByCategory(currentCategory)
            }
        } else {
            // 有搜索条件时
            if (currentCategory == "全部") {
                // 全部标签显示所有搜索结果
                searchResults
            } else {
                // 非全部标签下，根据分类过滤搜索结果
                searchResults.filter { herb ->
                    herb.category == currentCategory
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                // 当输入搜索内容时，自动切换到"全部"标签
                if (it.isNotEmpty()) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0) // 滚动到"全部"标签
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_herbs)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 可滚动的分类标签栏
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = {
                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }
        ) {
            allCategories.forEachIndexed { index, category ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { 
                        Text(
                            text = category,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Herbs List Title
        Text(
            text = "中药列表",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // HorizontalPager for swiping between categories
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { _ ->
            // 显示当前页面的药材列表
            if (filteredHerbs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("没有找到匹配的中药")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredHerbs) { herb ->
                        HerbListItem(herb, onHerbClick)
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun HerbListItem(herb: Herb, onHerbClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(vertical = 8.dp)
            .clickable { onHerbClick(herb.id) }
            .padding(16.dp)
    ) {
        Text(
            text = herb.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = herb.pinyin,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "归经: ${herb.meridians.joinToString(", ")}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "功效: ${herb.functions.joinToString(", ")}",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = herb.description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilledTonalButton(
            onClick = { onHerbClick(herb.id) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("查看详情")
        }
    }
} 