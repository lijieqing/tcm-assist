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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.tcm.traditionalchinesemedician.R
import com.tcm.traditionalchinesemedician.data.Herb
import com.tcm.traditionalchinesemedician.data.HerbRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HerbsScreen(
    selectedCategory: String? = null,
    initialSearchQuery: String = "",
    onHerbClick: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    val allCategories = listOf("全部") + HerbRepository.categories
    
    // 计算初始页面索引
    val initialPage = if (selectedCategory == "null" || selectedCategory == null) 0 
        else allCategories.indexOf(selectedCategory).coerceAtLeast(0)
    
    // Pager state for horizontal swiping
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { allCategories.size }
    )
    
    // 如果有初始搜索词，自动切换到"全部"标签
    LaunchedEffect(initialSearchQuery) {
        if (initialSearchQuery.isNotEmpty()) {
            pagerState.animateScrollToPage(0) // 滚动到"全部"标签
        }
    }
    
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
        
        Text(
            text = stringResource(R.string.search_helper_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                        HerbListItem(herb, searchQuery, onHerbClick)
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun highlightText(text: String, query: String): AnnotatedString {
    return buildAnnotatedString {
        if (query.isEmpty()) {
            append(text)
            return@buildAnnotatedString
        }
        
        val normalizedQuery = query.trim().lowercase()
        val normalizedText = text.lowercase()
        var startIndex = 0
        
        while (startIndex < text.length) {
            val matchIndex = normalizedText.indexOf(normalizedQuery, startIndex)
            if (matchIndex == -1) {
                // 如果没有找到匹配，添加剩余文本并结束
                append(text.substring(startIndex))
                break
            }
            
            // 添加匹配前的文本
            append(text.substring(startIndex, matchIndex))
            
            // 添加高亮的匹配文本
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )) {
                append(text.substring(matchIndex, matchIndex + normalizedQuery.length))
            }
            
            // 更新起始索引
            startIndex = matchIndex + normalizedQuery.length
        }
    }
}

@Composable
fun HerbListItem(herb: Herb, searchQuery: String, onHerbClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(vertical = 8.dp)
            .clickable { onHerbClick(herb.id) }
            .padding(16.dp)
    ) {
        Text(
            text = if (searchQuery.isNotEmpty() && herb.name.lowercase().contains(searchQuery.lowercase())) 
                    highlightText(herb.name, searchQuery) else AnnotatedString(herb.name),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = if (searchQuery.isNotEmpty() && herb.pinyin.lowercase().contains(searchQuery.lowercase())) 
                    highlightText(herb.pinyin, searchQuery) else AnnotatedString(herb.pinyin),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // 归经
        val meridiansText = "${stringResource(R.string.meridians)}: ${herb.meridians.joinToString(", ")}"
        Text(
            text = if (searchQuery.isNotEmpty() && meridiansText.lowercase().contains(searchQuery.lowercase())) 
                    highlightText(meridiansText, searchQuery) else AnnotatedString(meridiansText),
            style = MaterialTheme.typography.bodySmall
        )
        
        // 功效
        val functionsText = "${stringResource(R.string.functions)}: ${herb.functions.joinToString(", ")}"
        Text(
            text = if (searchQuery.isNotEmpty() && functionsText.lowercase().contains(searchQuery.lowercase())) 
                    highlightText(functionsText, searchQuery) else AnnotatedString(functionsText),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        // 主治
        val indicationsText = "${stringResource(R.string.indications)}: ${herb.indications.joinToString(", ")}"
        Text(
            text = if (searchQuery.isNotEmpty() && indicationsText.lowercase().contains(searchQuery.lowercase())) 
                    highlightText(indicationsText, searchQuery) else AnnotatedString(indicationsText),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 描述
        Text(
            text = if (searchQuery.isNotEmpty() && herb.description.lowercase().contains(searchQuery.lowercase())) 
                    highlightText(herb.description, searchQuery) else AnnotatedString(herb.description),
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