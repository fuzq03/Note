package com.king.easynote.presentation.share

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    @Composable
    fun captureAndSaveNote(view: ComposeView): ImageBitmap {
        return view.drawToBitmap().asImageBitmap()
    }

    private fun saveToGallery(bitmap: Bitmap, onSaved: (Boolean) -> Unit) {
        try {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val fileName = "Note_${System.currentTimeMillis()}.png"
            val file = File(dir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                onSaved(true)
            }
        } catch (e: Exception) {
            onSaved(false)
        }
    }
}