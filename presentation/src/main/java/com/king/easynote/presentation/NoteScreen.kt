package com.king.easynote.presentation

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Label
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.king.easynote.base.ui.theme.noteColors
import com.king.easynote.base.ui.theme.textColors
import com.king.easynote.domain.model.NoteType
import com.king.easynote.presentation.component.InputField
import com.king.easynote.presentation.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 笔记 - 保存（修改/增加）
 */
@Composable
fun NoteScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val state by viewModel.state

    // 添加分类下拉菜单
    var showCategoryDialog by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    val noteType = remember {
        navController.currentBackStackEntry?.arguments?.getString("type")
            ?.let { NoteType.valueOf(it) } ?: NoteType.TEXT
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("选择分类") },
            text = {
                Column {
                    state.categories.forEach { category ->
                        Text(
                            text = category,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onEvent(
                                        NoteViewModel.NoteEvent.SelectCategory(
                                            category
                                        )
                                    )
                                    showCategoryDialog = false
                                }
                                .padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCategoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }


    viewModel.onEvent(NoteViewModel.NoteEvent.SetType(noteType))

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest {
            when (it) {
                is NoteViewModel.UIEvent.ShowMessage -> {
                    it.message?.run {
                        scaffoldState.snackbarHostState.showSnackbar(message = this)
                    }

                }
                is NoteViewModel.UIEvent.SaveNote -> {
                    navController.navigateUp()
                }
            }
        }
    }

    when(noteType) {
        NoteType.TEXT -> TextNoteScreen(navController, viewModel, scaffoldState, showCategoryDialog) {
            showCategoryDialog = it
        }
        NoteType.IMAGE -> ImageNoteScreen(navController, viewModel, scaffoldState, showCategoryDialog) {
            showCategoryDialog = it
        }
        NoteType.AUDIO -> AudioNoteScreen(navController, viewModel, scaffoldState, showCategoryDialog) {
            showCategoryDialog = it
        }
    }


}

@Composable
fun NoteTopBar(
    title: String,
    hint: String,
    color: Color = Color.White,
    noteType: NoteType,
    onTitleChange: (String) -> Unit,
    navController: NavController,
    categories: List<String>,
    selectedCategory: String,
    isStarred: Boolean,
    onCategoryClick: () -> Unit,
    onStarClick: () -> Unit
) {
    val context = LocalContext.current
    var showPreview by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val view = LocalView.current

    // 存储权限请求Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && previewBitmap != null) {
            saveImageToGallery(context, previewBitmap!!)
        } else {
            Toast.makeText(context, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show()
        }
        showPreview = false
    }


    // 预览对话框
    if (showPreview && previewBitmap != null) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            title = { Text("笔记预览") },
            buttons = {
                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        saveImageToGallery(context, previewBitmap!!)
                        showPreview = false
                    } else {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }) {
                    Text("保存到相册")
                }
            },
            text = {
                previewBitmap?.let{
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "笔记预览"
                    )
                }
            }
        )
    }

    TopAppBar(
        title = { // 标题
            InputField(
                value = title,
                onValueChange = {
                    onTitleChange(it)
                },
                hint = hint,
                singleLine = true,
                textStyle = MaterialTheme.typography.h5,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            // 添加星标按钮
            IconButton(onClick = onStarClick) {
                Icon(
                    if (isStarred) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "星标"
                )
            }
            // 添加分类显示
            if (selectedCategory.isEmpty()) {
               IconButton(onClick = onCategoryClick) {
                   Icon(Icons.Outlined.Label, contentDescription = "选择分类")
               }
            } else {
                Text(
                    text = selectedCategory,
                    modifier = Modifier
                        .clickable(onClick = onCategoryClick)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            IconButton(onClick = {
                when(noteType) {
                    NoteType.AUDIO -> {
                        Toast.makeText(context, "音频类笔记暂不支持分享", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // 生成预览图

                        previewBitmap = captureView(view)
                        showPreview = true
                    }
                }
            }) {
                Icon(Icons.Default.Share, contentDescription = "分享")
            }
        },
        backgroundColor = color
    )
}

fun captureView(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(
        view.width,
        view.height,
        Bitmap.Config.ARGB_8888
    )
    view.draw(Canvas(bitmap))
    return bitmap
}

// 保存图片到相册
fun saveImageToGallery(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "note_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            context.contentResolver.openOutputStream(uri).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            true
        } ?: false
    } catch (e: Exception) {
        false
    }
}

@Composable
fun TextNoteScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    showCategoryDialog: Boolean,
    onShowCategoryDialogChange: (Boolean) -> Unit
){
    val viewState = viewModel.state.value

    var backgroundAnim = remember {
        Animatable(Color(viewState.color))
    }

    Scaffold(
        modifier = Modifier,
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(NoteViewModel.NoteEvent.SaveNote) },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(id = R.string.save)
                )
            }
        },
        topBar = {
                 NoteTopBar(
                     title = viewState.title,
                     hint = "文本笔记",
                     onTitleChange = { viewModel.onEvent(NoteViewModel.NoteEvent.ChangeTitle(it)) },
                     noteType = viewState.type,
                     navController = navController,
                     color = Color(viewState.color),
                     categories = viewState.categories,
                     selectedCategory = viewState.selectedCategory,
                     isStarred = viewState.isStarred,
                     onCategoryClick = { onShowCategoryDialogChange(!showCategoryDialog) },
                     onStarClick = { viewModel.onEvent(NoteViewModel.NoteEvent.ToggleStar(!viewState.isStarred))}
                 )

        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                noteColors.forEach { color ->
                    val colorInt = color.toArgb()
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(16.dp, CircleShape)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = 2.dp,
                                color = if (viewState.color == colorInt) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                viewModel.onEvent(NoteViewModel.NoteEvent.ChangeColor(colorInt))
                            })

                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundAnim.value)
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.padding(10.dp))
            // 内容
            InputField(
                value = viewState.content,
                onValueChange = {
                    viewModel.onEvent(NoteViewModel.NoteEvent.ChangeContent(it))
                },
                hint = stringResource(id = R.string.hint_note_content),
                textStyle = MaterialTheme.typography.h6.copy(color = textColors.get(Color(viewState.color)) ?: Color.White),
                modifier = Modifier
                    .padding(bottom = it.calculateBottomPadding())
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.Start),
            )
            Spacer(modifier = Modifier.padding(10.dp))
        }
    }

    LaunchedEffect(viewState.color){
        backgroundAnim.animateTo(
            targetValue = Color(viewState.color),
            animationSpec = tween()
        )
    }

}

@Composable
private fun ImageNoteScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    showCategoryDialog: Boolean,
    onShowCategoryDialogChange: (Boolean) -> Unit
) {
    val state = viewModel.state.value
    val context = LocalContext.current

    // 图片选择Launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onEvent(NoteViewModel.NoteEvent.AddImage(it.toString())) }
    }

    // 权限请求Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            // 显示权限被拒绝的提示
            Toast.makeText(context, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = {
                        // 这里应该调用图片选择器
                        // 检查权限
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            pickImageLauncher.launch("image/*")
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp),
                    backgroundColor = MaterialTheme.colors.secondary
                ) {
                    Icon(Icons.Default.Add, "添加图片")
                }
                FloatingActionButton(onClick = { viewModel.onEvent(NoteViewModel.NoteEvent.SaveNote) }) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "保存")
                }
            }
        },
        topBar = {
            NoteTopBar(
                title = viewModel.state.value.title,
                hint = "图片笔记",
                onTitleChange = { viewModel.onEvent(NoteViewModel.NoteEvent.ChangeTitle(it)) },
                noteType = viewModel.state.value.type,
                navController = navController,
                categories = viewModel.state.value.categories,
                selectedCategory = viewModel.state.value.selectedCategory,
                isStarred = viewModel.state.value.isStarred,
                onCategoryClick = { onShowCategoryDialogChange(!showCategoryDialog) },
                onStarClick = { viewModel.onEvent(NoteViewModel.NoteEvent.ToggleStar(!viewModel.state.value.isStarred))}
            )
        }
    ) {
        viewModel.onEvent(NoteViewModel.NoteEvent.ChangeTitle("图片笔记"))
        val scope = rememberCoroutineScope()
        Column(Modifier.padding(it)) {
            LazyRow {
                // 添加占位图项
                item {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clickable {
                                // 检查权限
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    pickImageLauncher.launch("image/*")
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "添加图片",
                                modifier = Modifier.size(48.dp)
                            )
                            Text("点击插入图片")
                        }
                    }
                }
                //显示已选图片
                items(state.images.size) {index ->
                    val image = state.images[index]
                    Image(
                        painter = rememberImagePainter(
                            data = image,
                            builder = {
                                crossfade(true)
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(onLongPress = {
                                    scope.launch {
                                        viewModel.onEvent(NoteViewModel.NoteEvent.RemoveImage(image))
                                    }
                                })
                            }
                    )
                }
            }

            TextField(
                value = state.imageCaption,
                onValueChange = {viewModel.onEvent(NoteViewModel.NoteEvent.UpdateImageCaption(it))},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun AudioNoteScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    showCategoryDialog: Boolean,
    onShowCategoryDialogChange: (Boolean) -> Unit
) {
    val state = viewModel.state.value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setContext(context)
    }

    // 录音权限检查
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onEvent(NoteViewModel.NoteEvent.StartRecording)
        } else {
            Toast.makeText(context, "需要录音权限", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            Column {
                // 录音按钮
                FloatingActionButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            if (state.isRecording) {
                                viewModel.onEvent(NoteViewModel.NoteEvent.StopRecording)
                            } else {
                                viewModel.onEvent(NoteViewModel.NoteEvent.StartRecording)
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    backgroundColor = if (state.isRecording) Color.Red else MaterialTheme.colors.secondary
                ) {
                    Icon(
                        if (state.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        if (state.isRecording) "停止录音" else "开始录音"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // 保存按钮
                FloatingActionButton(onClick = { viewModel.onEvent(NoteViewModel.NoteEvent.SaveNote) }) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "保存")
                }
            }
        },
        topBar = {
            NoteTopBar(
                title = viewModel.state.value.title,
                hint = "音频笔记",
                onTitleChange = { viewModel.onEvent(NoteViewModel.NoteEvent.ChangeTitle(it)) },
                noteType = viewModel.state.value.type,
                navController = navController,
                categories = viewModel.state.value.categories,
                selectedCategory = viewModel.state.value.selectedCategory,
                isStarred = viewModel.state.value.isStarred,
                onCategoryClick = { onShowCategoryDialogChange(!showCategoryDialog) },
                onStarClick = { viewModel.onEvent(NoteViewModel.NoteEvent.ToggleStar(!viewModel.state.value.isStarred))}
            )
        }
    ) {
        viewModel.onEvent(NoteViewModel.NoteEvent.ChangeTitle("音频笔记"))
        Column(Modifier.padding(it)) {
            // 播放控制
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (state.isPlaying) {
                        viewModel.onEvent(NoteViewModel.NoteEvent.PauseAudio)
                    } else {
                        viewModel.onEvent(NoteViewModel.NoteEvent.PlayAudio)
                    }
                }) {
                    Icon(
                        if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (state.isPlaying) "暂停" else "播放"
                    )
                }
                Text("${state.duration}秒")
            }

            // 音频备注
            TextField(
                value = state.audioNote,
                onValueChange = { viewModel.onEvent(NoteViewModel.NoteEvent.UpdateAudioNote(it)) },
                label = { Text("音频备注") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
