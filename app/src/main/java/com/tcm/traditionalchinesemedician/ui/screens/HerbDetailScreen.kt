package com.tcm.traditionalchinesemedician.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcm.traditionalchinesemedician.data.Herb
import com.tcm.traditionalchinesemedician.data.HerbRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbDetailScreen(
    herbId: Int,
    onBackPressed: () -> Unit
) {
    val herb = HerbRepository.getAllHerbs().find { it.id == herbId }
    
    herb?.let {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(herb.name) },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Add to favorites */ }) {
                            Icon(Icons.Filled.Favorite, contentDescription = "收藏")
                        }
                        IconButton(onClick = { /* Share */ }) {
                            Icon(Icons.Filled.Share, contentDescription = "分享")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                HerbHeader(herb)
                
                HerbContent(herb)
            }
        }
    } ?: run {
        // Herb not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("未找到中药信息")
        }
    }
}

@Composable
fun HerbHeader(herb: Herb) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Herb image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = herb.name,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = herb.pinyin,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            AssistChip(
                onClick = { },
                label = { Text(herb.category) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Category,
                        contentDescription = null,
                        Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Divider()
    }
}

@Composable
fun HerbContent(herb: Herb) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Basic properties
        ContentSection(title = "基本属性") {
            PropertyItem(label = "性味", value = herb.properties + "，" + herb.taste)
            PropertyItem(label = "归经", value = herb.meridians.joinToString("、"))
        }
        
        // Functions and indications
        ContentSection(title = "功效与主治") {
            Text(
                text = "功效",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            herb.functions.forEach { function ->
                Text(
                    text = "• $function",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "主治",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            herb.indications.forEach { indication ->
                Text(
                    text = "• $indication",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
        
        // Usage information
        ContentSection(title = "用法用量") {
            PropertyItem(label = "用量", value = herb.dosage)
            PropertyItem(label = "禁忌", value = herb.contraindications)
        }
        
        // Detailed description
        ContentSection(title = "详细介绍") {
            Text(
                text = herb.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Related herbs (placeholder)
        ContentSection(title = "相关中药") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RelatedHerbItem("人参")
                RelatedHerbItem("黄芪")
                RelatedHerbItem("党参")
            }
        }
    }
}

@Composable
fun ContentSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun PropertyItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(56.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatedHerbItem(herbName: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = { /* Navigate to herb */ }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = herbName,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
} 