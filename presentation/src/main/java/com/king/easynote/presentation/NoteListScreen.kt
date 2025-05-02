package com.king.easynote.presentation

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.king.easynote.base.ui.theme.HintColor
import com.king.easynote.base.ui.theme.TitleColor
import com.king.easynote.domain.model.NoteType
import com.king.easynote.presentation.component.InputField
import com.king.easynote.presentation.component.NoteItem
import com.king.easynote.presentation.navigation.NavRoute
import com.king.easynote.presentation.viewmodel.NoteListViewModel
import com.king.easynote.presentation.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch

/**
 * 笔记列表
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@Composable
fun NoteListScreen(
    navController: NavController,
    viewModel: NoteListViewModel = hiltViewModel()
) {

    //控制选择笔记类型对话框是否显示
    var showNoteTypeDialog by remember {
        mutableStateOf(false) }
    val selectedTag = viewModel.selectedTag.value ?: "全部"

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showNoteTypeDialog = true
                    //navController.navigate(NavRoute.NoteRoute.route)
                },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add)
                )
            }
        }

    ) {
        if (showNoteTypeDialog) {
            NoteTypeSelectionDialog(
                onDismiss = {showNoteTypeDialog = false},
                onConfirm = {
                    type -> showNoteTypeDialog = false
                    navController.navigate(NavRoute.NoteRoute.route + "?type=${type.name}")
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = it.calculateBottomPadding())
        ) {
            TopBar(navController, viewModel)
            NoteListContent(navController, viewModel)
        }
    }

}

/**
 * 选择笔记类型对话框
 */
@Composable
private fun NoteTypeSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (NoteType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("选择笔记类型")},
        buttons = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .height(150.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {onConfirm(NoteType.TEXT)},
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.8f)
                ) {
                    Text(text = "文本笔记")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {onConfirm(NoteType.IMAGE)},
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.8f)
                ) {
                    Text("图片笔记")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {onConfirm(NoteType.AUDIO)},
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.8f)
                ) {
                    Text("音频笔记")
                }
            }

        }
    )
}

/**
 * 顶部标题栏
 */
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun TopBar(navController: NavController, viewModel: NoteListViewModel, noteViewModel: NoteViewModel = hiltViewModel()) {
    var expanded by remember { mutableStateOf(false) }
    var allTags by remember { mutableStateOf(listOf("全部")) }

    LaunchedEffect(Unit) {
        val categories = noteViewModel.getCategories()
        allTags = listOf("全部") + categories
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { expanded = true }) {
            viewModel.selectedTag.value?.let {
                Text(
                    text = it,
                    modifier = Modifier.clickable {  }
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "展开标签列表",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allTags?.forEach { tag ->
                DropdownMenuItem(
                    onClick = {
                        viewModel.updateSelectedTag(tag)
                        expanded = false
                    }
                ) {
                    Text(text = tag)
                }
            }
        }
        IconButton(
            onClick = { navController.navigate(NavRoute.SettingsRoute.route) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colors.primary
            )
        }
    }

}

/**
 * 笔记列表内容
 */
@Composable
fun NoteListContent(
    navController: NavController,
    viewModel: NoteListViewModel
) {
    val state = viewModel.state
    val noteList = state.value.notes
    // 搜索框
    InputField(
        value = state.value.text,
        onValueChange = {
            viewModel.onEvent(NoteListViewModel.NoteListEvent.SearchNote(it))
        },
        hint = stringResource(id = R.string.hint_note_search),
        singleLine = true,
        textStyle = MaterialTheme.typography.h5,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )

    if (noteList.isNotEmpty()) {
        Spacer(modifier = Modifier.padding(8.dp))
        // 列表项
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(noteList) { note ->
                NoteItem(
                    note = note,
                    onNoteClick = { noteId ->
                        viewModel.onEvent(NoteListViewModel.NoteListEvent.OpenNote(noteId))
                        navController.navigate(NavRoute.NoteRoute.navigateRoute(note.id, note.type))
                    },
                    modifier = Modifier.padding(8.dp),
                    onDeleteClick = {
                        navController.navigate(NavRoute.DeleteNoteDialogRoute.navigateRoute(note.id))
                    }
                )
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
            }
            // 让底部能向上滑一段距离，避免被遮盖
            item {
                Spacer(modifier = Modifier.padding(bottom = 60.dp))
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.content_is_empty), color = HintColor)
        }
    }
}

