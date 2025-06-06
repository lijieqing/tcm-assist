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
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbDetailScreen(
    herbId: Int,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { HerbRepository.getInstance(context) }
    var herb by remember { mutableStateOf<Herb?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load herb details
    LaunchedEffect(herbId) {
        isLoading = true
        herb = repository.getHerbById(herbId)
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(herb?.name ?: "中药详情") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (herb != null) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                HerbHeader(herb!!)
                
                HerbContent(herb!!)
            }
        } else {
            // Herb not found
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("未找到中药信息")
            }
        }
    }
}

@Composable
fun HerbHeader(herb: Herb) {
    val uriHandler = LocalUriHandler.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Herb image
        if (herb.images != null && herb.images.isNotEmpty()) {
            // TODO: 实现图片加载逻辑
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
        } else {
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
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = herb.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                herb.url?.let { url ->
                    IconButton(onClick = { uriHandler.openUri(url) }) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = "查看详情页",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            herb.pinYin?.let { pinYin ->
                Text(
                    text = pinYin,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
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
        // 基本属性（只保留药用部位和性味归经）
        ContentSection(title = "基本属性") {
            herb.medicinalPart?.let { 
                PropertyItem(label = "药用部位", value = it)
            }
            
            herb.tasteMeridian?.let {
                PropertyItem(label = "性味归经", value = it)
            }
        }
        
        // 功效与临床应用（移除功效的显示）
        ContentSection(title = "功效与临床应用") {
            herb.functions?.let { functions ->
                Text(
                    text = "功效分类",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FunctionsTagsRow(functions, MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            herb.clinicalApplication?.let {
                Text(
                    text = "临床应用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ClinicalApplicationsTagsRow(it, MaterialTheme.colorScheme.tertiary)
            }
        }
        
        // 用法用量
        ContentSection(title = "用法用量") {
            herb.prescriptionName?.let {
                PropertyItem(label = "处方用名", value = it)
            }
            
            herb.usageDosage?.let {
                PropertyItem(label = "用法用量", value = it)
            }
        }
        
        // 附注说明
        herb.notes?.let {
            if (it.isNotEmpty()) {
                ContentSection(title = "附注说明") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        it.forEach { note ->
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
        
        // 方剂举例
        herb.formulas?.let {
            if (it.isNotEmpty()) {
                ContentSection(title = "方剂举例") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        it.forEach { formula ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = formula,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 文献摘录（无内容时隐藏标题）
        herb.literature?.let {
            if (it.isNotEmpty()) {
                ContentSection(title = "文献摘录") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        it.forEach { literature ->
                            Text(
                                text = literature,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FunctionsTagsRow(functions: List<String>?, baseColor: Color) {
    functions?.let { functionsList ->
        if (functionsList.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                functionsList.forEach { function ->
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClinicalApplicationsTagsRow(clinicalApplications: List<String>?, baseColor: Color) {
    clinicalApplications?.let { applicationsList ->
        if (applicationsList.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                applicationsList.forEach { application ->
                    SuggestionChip(
                        onClick = { },
                        label = { 
                            Text(
                                text = application,
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        icon = {
                            Icon(
                                Icons.Filled.Info,
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
fun PropertyItem(label: String, value: String?) {
    value?.let {
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
                modifier = Modifier.width(72.dp)
            )
            
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
} 