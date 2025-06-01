package com.tcm.traditionalchinesemedician.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        SettingsSection()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SettingItem(
                icon = Icons.Filled.Notifications,
                title = "通知设置",
                subtitle = "管理学习提醒和通知"
            )
            
            SettingItem(
                icon = Icons.Filled.Brightness6,
                title = "显示设置",
                subtitle = "深色模式与字体大小"
            )
            
            SettingItem(
                icon = Icons.Filled.Language,
                title = "语言",
                subtitle = "简体中文"
            )
            
            SettingItem(
                icon = Icons.Filled.Info,
                title = "关于应用",
                subtitle = "版本 1.0.0"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { /* Clear cache */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("清除缓存")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String) {
    Surface(
        onClick = { /* Navigate to setting */ },
        color = MaterialTheme.colorScheme.surface
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            headlineContent = { 
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        )
    }
    
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
} 