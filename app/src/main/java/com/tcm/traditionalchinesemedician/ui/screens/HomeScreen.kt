package com.tcm.traditionalchinesemedician.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.tcm.traditionalchinesemedician.R
import com.tcm.traditionalchinesemedician.data.HerbRepository
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import com.google.accompanist.flowlayout.FlowRow
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onHerbClick: (Int) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchTermClick: (String) -> Unit = onCategoryClick // 默认复用onCategoryClick函数
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome section with header
        Text(
            text = stringResource(R.string.welcome_message),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Featured Content Section
        Text(
            text = stringResource(R.string.daily_recommendation),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        FeaturedHerbsSection(onHerbClick)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Categories Section
        Text(
            text = stringResource(R.string.categories),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        CategoriesSection(onCategoryClick)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 功效分类
        Text(
            text = stringResource(R.string.common_functions),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // 显示8个功效标签，使用缓存的随机推荐
        val functions = HerbRepository.recommendedFunctions
        FlowTagRow(
            items = functions,
            onItemClick = { function -> onSearchTermClick(function) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 主治分类
        Text(
            text = stringResource(R.string.common_indications),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // 显示8个主治标签，使用缓存的随机推荐
        val indications = HerbRepository.recommendedIndications
        FlowTagRow(
            items = indications,
            onItemClick = { indication -> onSearchTermClick(indication) }
        )
    }
}

@Composable
fun FeaturedHerbsSection(onHerbClick: (Int) -> Unit) {
    val featuredHerbs = HerbRepository.getAllHerbs().take(3)
    
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(featuredHerbs) { herb ->
            ElevatedCard(
                modifier = Modifier
                    .width(260.dp)
                    .clickable { onHerbClick(herb.id) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = herb.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = herb.pinyin,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = herb.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesSection(onCategoryClick: (String) -> Unit) {
    // 使用FlowRow布局展示所有分类
    val categories = HerbRepository.categories
    
    FlowTagRow(
        items = categories,
        onItemClick = onCategoryClick
    )
}

// 自定义流式布局组件，用于显示标签
@Composable
fun FlowTagRow(
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        items.forEach { item ->
            TagItem(
                text = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

// 通用标签项组件
@Composable
fun TagItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
} 