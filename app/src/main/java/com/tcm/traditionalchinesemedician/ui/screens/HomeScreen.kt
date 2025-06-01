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
import com.tcm.traditionalchinesemedician.R
import com.tcm.traditionalchinesemedician.data.HerbRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onHerbClick: (Int) -> Unit,
    onCategoryClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    val filteredHerbs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            HerbRepository.searchHerbs(searchQuery)
        }
    }
    
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
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                isSearchActive = it.isNotEmpty()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_herbs)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Search results
        if (isSearchActive) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "搜索结果",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    if (filteredHerbs.isEmpty() && searchQuery.isNotEmpty()) {
                        Text(
                            text = "未找到匹配的中药",
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        filteredHerbs.take(3).forEach { herb ->
                            ListItem(
                                headlineContent = { 
                                    Text(
                                        text = herb.name,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    ) 
                                },
                                supportingContent = { 
                                    Text(
                                        text = herb.pinyin,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    ) 
                                },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onHerbClick(herb.id) }
                                    .background(Color.Transparent)
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        
                        if (filteredHerbs.size > 3) {
                            TextButton(
                                onClick = { 
                                    onCategoryClick("全部")
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("查看更多结果")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Featured Content Section
        Text(
            text = "每日推荐",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        FeaturedHerbsSection(onHerbClick)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Categories Section
        Text(
            text = "中药分类",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        CategoriesSection(onCategoryClick)
    }
}

@Composable
fun FeaturedHerbsSection(onHerbClick: (Int) -> Unit) {
    val featuredHerbs = HerbRepository.herbs.take(3)
    
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
    val categories = listOf("补气类", "补血类", "清热类", "解表类", "活血类", "利水渗湿药")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        categories.take(3).forEach { category ->
            CategoryCard(
                categoryName = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        categories.drop(3).forEach { category ->
            CategoryCard(
                categoryName = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryCard(categoryName: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
} 