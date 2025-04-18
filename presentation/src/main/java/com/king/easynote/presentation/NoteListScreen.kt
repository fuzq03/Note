package com.king.easynote.presentation

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
import androidx.navigation.NavController
import com.king.easynote.base.ui.theme.HintColor
import com.king.easynote.base.ui.theme.TitleColor
import com.king.easynote.domain.model.NoteType
import com.king.easynote.presentation.component.InputField
import com.king.easynote.presentation.component.NoteItem
import com.king.easynote.presentation.navigation.NavRoute
import com.king.easynote.presentation.viewmodel.NoteListViewModel

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
            TopBar(navController)
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
                modifier = Modifier.padding(8.dp).height(150.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {onConfirm(NoteType.TEXT)},
                    modifier = Modifier.weight(1f).fillMaxWidth(0.8f)
                ) {
                    Text(text = "文本笔记")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {onConfirm(NoteType.IMAGE)},
                    modifier = Modifier.weight(1f).fillMaxWidth(0.8f)
                ) {
                    Text("图片笔记")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {onConfirm(NoteType.AUDIO)},
                    modifier = Modifier.weight(1f).fillMaxWidth(0.8f)
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
@Composable
private fun TopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.note_title),
            color = TitleColor,
            style = MaterialTheme.typography.h5
        )
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
                        navController.navigate(NavRoute.NoteRoute.navigateRoute(note.id, note.type))
                    },
                    modifier = Modifier.padding(8.dp)
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

