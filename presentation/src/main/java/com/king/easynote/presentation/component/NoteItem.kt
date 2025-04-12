package com.king.easynote.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.rememberImagePainter
import com.king.easynote.base.ui.theme.ContentColor
import com.king.easynote.base.ui.theme.TitleColor
import com.king.easynote.domain.model.Note
import com.king.easynote.domain.model.NoteType
import com.king.easynote.presentation.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 笔记 - 列表项
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@Composable
fun NoteItem(
    note: Note,
    modifier: Modifier = Modifier,
    onNoteClick: (Int) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNoteClick(note.id ?: 0) },
        backgroundColor = Color(note.color),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.h6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (note.type) {
                NoteType.IMAGE -> {
                    if (note.images.isNotEmpty()) {
                        Image(
                            painter = rememberImagePainter(
                                data = note.images.first(),
                                builder = { crossfade(true) }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.body2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                NoteType.AUDIO -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "播放",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${note.duration}秒",
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.body2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                else -> {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.body2,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(note.created)),
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
        }
    }
}
