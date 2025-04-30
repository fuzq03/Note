package com.king.easynote.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.king.easynote.domain.model.CategoryEntity
import com.king.easynote.presentation.viewmodel.NoteViewModel
import com.king.easynote.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 这里添加设置项
            Text("主题设置", style = MaterialTheme.typography.h6)
            // 可以添加更多设置项...
            // 标签管理项
            TextButton(
                onClick = {
                    navController.navigate("tagManagement")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("标签管理", style = MaterialTheme.typography.h6)
            }
        }
    }
}

@Composable
fun TagManagementScreen(navController: NavController, viewModel: NoteViewModel = hiltViewModel()) {

    var newTag by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(listOf<CategoryEntity>()) }

    LaunchedEffect(Unit) {
        viewModel.getAllCategories().collectLatest {
            categories = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 返回按钮
        Button(onClick = { navController.popBackStack() }) {
            Text("返回")
        }

        // 输入新标签的输入框
        TextField(
            value = newTag,
            onValueChange = { newTag = it },
            label = { Text("输入新标签") },
            modifier = Modifier.fillMaxWidth()
        )

        // 添加标签按钮
        Button(
            onClick = {
                if (newTag.isNotBlank()) {
                    viewModel.saveCategory(categories.size, newTag)
                    newTag = ""
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text("添加标签")
        }

        // 显示标签列表
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            categories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.name, modifier = Modifier.weight(1f))
                    // 删除标签按钮
                    Button(
                        onClick = {
                            viewModel.deleteCategory(category.name)
                        }
                    ) {
                        Text("删除")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}