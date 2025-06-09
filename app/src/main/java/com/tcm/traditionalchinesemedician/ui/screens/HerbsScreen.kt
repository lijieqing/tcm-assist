package com.tcm.traditionalchinesemedician.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.tcm.traditionalchinesemedician.R
import com.tcm.traditionalchinesemedician.data.Herb
import com.tcm.traditionalchinesemedician.data.HerbRepository
import com.tcm.traditionalchinesemedician.ui.viewmodels.HerbsViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HerbsScreen(
    viewModel: HerbsViewModel,
    onHerbClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { query -> 
                // 只更新搜索内容，不触发搜索操作
                viewModel.updateSearchQuery(query)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_herbs)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "搜索",
                        modifier = Modifier.clickable { 
                            // 点击搜索按钮时执行搜索
                            viewModel.performSearch() 
                        }
                    )
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            // 添加键盘操作监听，按下回车键触发搜索
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { viewModel.performSearch() }
            )
        )
        
        Text(
            text = stringResource(R.string.search_helper_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 可滚动的分类标签栏 - 仅在有类别数据时显示
        if (uiState.categories.isNotEmpty()) {
            val allCategories = listOf("全部") + uiState.categories
            
            // 计算当前应该选中的tab索引
            val selectedIndex = if (uiState.selectedCategory == null) {
                0 // "全部" index
            } else {
                val index = allCategories.indexOf(uiState.selectedCategory)
                if (index >= 0) index else 0
            }
            
            // 创建pagerState，设置初始页为当前选中索引
            val pagerState = rememberPagerState(
                initialPage = selectedIndex,
                pageCount = { allCategories.size }
            )
            
            // 监听分页器页面变化，仅更新选中的分类
            LaunchedEffect(pagerState.currentPage) {
                val page = pagerState.currentPage
                val category = if (page == 0) null else allCategories[page]
                
                // 只有当分类真正变化时才更新
                if (category != uiState.selectedCategory) {
                    viewModel.updateSelectedCategory(category)
                    // 使用当前的搜索内容和新的分类加载数据
                    viewModel.loadDataWithCurrentState()
                }
            }
            
            // 监听UI状态中选中分类的变化，更新分页器页面
            LaunchedEffect(uiState.selectedCategory) {
                val categoryIndex = if (uiState.selectedCategory == null) 0
                                    else allCategories.indexOf(uiState.selectedCategory).coerceAtLeast(0)
                
                // 只有当分页器页面与选中分类不一致时才滚动
                if (pagerState.currentPage != categoryIndex) {
                    pagerState.animateScrollToPage(categoryIndex)
                }
            }
            
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {
                    Divider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            ) {
                val coroutineScope = rememberCoroutineScope()
                
                allCategories.forEachIndexed { index, category ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = {
                            // 使用协程启动分页动画
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (index == pagerState.currentPage) 
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            // 分页内容区域
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { it } // 添加key参数以解决重用问题
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                ) {
                    when {
                        uiState.isLoading && uiState.herbs.isEmpty() -> {
                            // 仅在初始加载时显示加载指示器
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        uiState.error != null -> {
                            // 显示错误状态
                            HerbsErrorState(
                                error = uiState.error!!,
                                onRetry = {
                                    // 重试时使用当前状态加载数据
                                    viewModel.loadDataWithCurrentState()
                                },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        uiState.herbs.isEmpty() -> {
                            // 显示空状态
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_herbs_found),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> {
                            // 显示药材列表
                            val lazyListState = rememberLazyListState()
                            
                            // 监听列表滚动到底部以加载更多
                            LaunchedEffect(lazyListState) {
                                snapshotFlow { 
                                    val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                    val totalItems = lazyListState.layoutInfo.totalItemsCount
                                    lastVisibleItem >= totalItems - 5 // 预加载阈值
                                }.collect { shouldLoadMore ->
                                    if (shouldLoadMore && !uiState.isLoading && uiState.hasMoreData) {
                                        viewModel.loadMoreHerbs()
                                    }
                                }
                            }
                            
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = uiState.herbs,
                                    key = { herb -> herb.id } // 添加key参数以唯一标识每个项目
                                ) { herb ->
                                    HerbItem(
                                        herb = herb,
                                        searchQuery = uiState.searchQuery,
                                        onClick = { onHerbClick(herb.id) }
                                    )
                                    Divider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        thickness = 1.dp
                                    )
                                }
                                
                                // 底部加载更多指示器
                                if (uiState.isLoading && uiState.herbs.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                                
                                // 没有更多数据提示
                                if (!uiState.hasMoreData && uiState.herbs.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "已加载全部数据",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (uiState.isLoading) {
            // 分类数据加载中
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun HerbsErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
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
fun HerbItem(herb: Herb, searchQuery: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // 药材名称
        Text(
            text = if (searchQuery.isNotEmpty() && herb.name.lowercase().contains(searchQuery.lowercase())) 
                    highlightText(herb.name, searchQuery) else AnnotatedString(herb.name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // 拼音
        herb.pinYin?.let { pinYin ->
            Text(
                text = if (searchQuery.isNotEmpty() && pinYin.lowercase().contains(searchQuery.lowercase())) 
                        highlightText(pinYin, searchQuery) else AnnotatedString(pinYin),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 性味归经
        herb.tasteMeridian?.let {
            val tasteMeridianText = "${stringResource(R.string.taste_meridian)}: $it"
            Text(
                text = if (searchQuery.isNotEmpty() && tasteMeridianText.lowercase().contains(searchQuery.lowercase())) 
                        highlightText(tasteMeridianText, searchQuery) else AnnotatedString(tasteMeridianText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 药用部位
        herb.medicinalPart?.let {
            val medicinalPartText = "${stringResource(R.string.medicinal_part)}: $it"
            Text(
                text = if (searchQuery.isNotEmpty() && medicinalPartText.lowercase().contains(searchQuery.lowercase())) 
                        highlightText(medicinalPartText, searchQuery) else AnnotatedString(medicinalPartText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // 功效描述
        herb.effects?.let {
            val effectsText = "${stringResource(R.string.effects)}: $it"
            Text(
                text = if (searchQuery.isNotEmpty() && effectsText.lowercase().contains(searchQuery.lowercase())) 
                        highlightText(effectsText, searchQuery) else AnnotatedString(effectsText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 