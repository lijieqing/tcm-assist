package com.tcm.traditionalchinesemedician.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tcm.traditionalchinesemedician.data.Herb
import com.tcm.traditionalchinesemedician.data.HerbPairing
import com.tcm.traditionalchinesemedician.data.HerbRepository
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

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
                imageVector = Icons.Filled.LocalPharmacy,
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
                        Icons.Filled.LocalPharmacy,
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            FunctionsTagsRow(herb.functions, MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "主治",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            IndicationsTagsRow(herb.indications, MaterialTheme.colorScheme.tertiary)
        }
        
        // Usage information
        ContentSection(title = "用法用量") {
            PropertyItem(label = "用量", value = herb.dosage)
            PropertyItem(label = "用法", value = herb.usage)
            PropertyItem(label = "禁忌", value = herb.contraindications)
        }
        
        // Common pairings section with tags
        CommonPairingsSection(herb.commonPairings)
        
        // Detailed description
        ContentSection(title = "详细介绍") {
            Text(
                text = herb.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FunctionsTagsRow(functions: List<String>, baseColor: androidx.compose.ui.graphics.Color) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        functions.forEach { function ->
            SuggestionChip(
                onClick = { },
                label = { 
                    Text(
                        text = function,
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                icon = {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = baseColor
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = baseColor.copy(alpha = 0.08f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    borderColor = baseColor.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IndicationsTagsRow(indications: List<String>, baseColor: androidx.compose.ui.graphics.Color) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        indications.forEach { indication ->
            SuggestionChip(
                onClick = { },
                label = { 
                    Text(
                        text = indication,
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                icon = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = baseColor
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = baseColor.copy(alpha = 0.08f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    borderColor = baseColor.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommonPairingsSection(pairings: List<HerbPairing>) {
    var selectedPairing by remember { mutableStateOf<HerbPairing?>(null) }
    
    ContentSection(title = "常见配伍") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pairings.forEach { pairing ->
                ElevatedAssistChip(
                    onClick = { selectedPairing = pairing },
                    label = { 
                        Text(
                            text = pairing.name,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.LocalPharmacy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = AssistChipDefaults.elevatedAssistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
    
    // Dialog to show pairing details
    selectedPairing?.let { pairing ->
        Dialog(onDismissRequest = { selectedPairing = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = pairing.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    PropertyItem(label = "用法", value = pairing.usage)
                    PropertyItem(label = "功效", value = pairing.effect)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { selectedPairing = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("关闭")
                    }
                }
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