package com.tcm.traditionalchinesemedician.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.tcm.traditionalchinesemedician.R
import com.tcm.traditionalchinesemedician.data.Herb
import com.tcm.traditionalchinesemedician.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onHerbClick: (Int) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchTermClick: (String) -> Unit = onCategoryClick, // 默认复用onCategoryClick函数
    viewModel: HomeViewModel = viewModel()
) {
    // Get UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            uiState.isLoading -> {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.error != null -> {
                // Show error state with retry button
                HomeErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.retryLoading() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                // Show content when data is loaded
                HomeContent(
                    featuredHerbs = uiState.featuredHerbs,
                    categories = uiState.categories,
                    recommendedFunctions = uiState.recommendedFunctions,
                    recommendedClinicalApplications = uiState.recommendedClinicalApplications,
                    onHerbClick = onHerbClick,
                    onCategoryClick = onCategoryClick,
                    onSearchTermClick = onSearchTermClick
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    featuredHerbs: List<Herb>,
    categories: List<String>,
    recommendedFunctions: List<String>,
    recommendedClinicalApplications: List<String>,
    onHerbClick: (Int) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchTermClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
        
        if (featuredHerbs.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.no_featured_herbs),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        } else {
            FeaturedHerbsSection(featuredHerbs, onHerbClick)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Categories Section
        Text(
            text = stringResource(R.string.categories),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        if (categories.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.no_categories),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        } else {
            CategoriesSection(categories, onCategoryClick)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 功效分类
        Text(
            text = stringResource(R.string.common_functions),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        if (recommendedFunctions.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.no_functions),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        } else {
            // 显示功效标签
            FlowTagRow(
                items = recommendedFunctions,
                onItemClick = { function -> onSearchTermClick(function) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 临床应用分类
        Text(
            text = stringResource(R.string.common_indications),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        if (recommendedClinicalApplications.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.no_clinical_applications),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        } else {
            // 显示临床应用标签
            FlowTagRow(
                items = recommendedClinicalApplications,
                onItemClick = { application -> onSearchTermClick(application) }
            )
        }
    }
}

@Composable
fun FeaturedHerbsSection(featuredHerbs: List<Herb>, onHerbClick: (Int) -> Unit) {
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
                    herb.pinYin?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 显示药材功效描述
                    herb.effects?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3
                        )
                    }
                    
                    // 显示药用部位（如果effects为null）
                    if (herb.effects == null) {
                        herb.medicinalPart?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesSection(categories: List<String>, onCategoryClick: (String) -> Unit) {
    // 使用FlowRow布局展示所有分类
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

@Composable
fun HomeErrorState(
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
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
} 