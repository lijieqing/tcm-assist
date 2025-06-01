package com.tcm.traditionalchinesemedician.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbsScreen(
    selectedCategory: String? = null,
    onHerbClick: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentCategory by remember { mutableStateOf(selectedCategory ?: "全部") }
    
    val allCategories = listOf("全部") + HerbRepository.categories
    
    // Filter herbs based on search query and selected category
    val filteredHerbs = remember(searchQuery, currentCategory) {
        val baseList = if (currentCategory == "全部") {
            HerbRepository.herbs
        } else {
            HerbRepository.herbs.filter { herb -> 
                herb.category == currentCategory ||
                herb.category.contains(currentCategory)
            }
        }
        
        if (searchQuery.isEmpty()) {
            baseList
        } else {
            baseList.filter { herb ->
                herb.name.contains(searchQuery) || 
                herb.pinyin.contains(searchQuery, ignoreCase = true)
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
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_herbs)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category Chips
        ScrollableTabRow(
            selectedTabIndex = allCategories.indexOf(currentCategory).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            allCategories.forEachIndexed { _, category ->
                Tab(
                    selected = currentCategory == category,
                    onClick = { currentCategory = category },
                    text = { Text(category) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Herbs List
        Text(
            text = "中药列表",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (filteredHerbs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("没有找到匹配的中药")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(filteredHerbs) { herb ->
                    HerbListItem(herb, onHerbClick)
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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