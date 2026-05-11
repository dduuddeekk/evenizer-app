package com.dudek.evenizer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun cropImage(
        context: Context,
        uri: Uri,
        scale: Float,
        offset: Offset,
        containerSizePx: Float
    ): Uri? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            
            // Create a target square bitmap
            val targetSize = 512 // Standardize upload size
            val croppedBitmap = createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(croppedBitmap)
            
            val matrix = Matrix()
            
            // 1. Initial Scale to Fit (simulating ContentScale.Fit)
            val baseScale = containerSizePx / maxOf(originalBitmap.width, originalBitmap.height)
            val initialWidth = originalBitmap.width * baseScale
            val initialHeight = originalBitmap.height * baseScale
            
            // Center it in the target coordinates (which are mapped from container space)
            val dx = (containerSizePx - initialWidth) / 2f
            val dy = (containerSizePx - initialHeight) / 2f
            
            // 2. Apply User Transformations
            // Matrix operations: Order is important.
            // Move to center, apply user offset, apply user scale around the center of the box.
            matrix.postScale(baseScale, baseScale)
            matrix.postTranslate(dx, dy)
            
            // Apply zoom around the center of the container
            matrix.postTranslate(-containerSizePx / 2f, -containerSizePx / 2f)
            matrix.postScale(scale, scale)
            matrix.postTranslate(containerSizePx / 2f, containerSizePx / 2f)
            
            // Apply user panning
            matrix.postTranslate(offset.x, offset.y)
            
            // Final Scale to standard upload size (targetSize)
            val finalScale = targetSize.toFloat() / containerSizePx
            matrix.postScale(finalScale, finalScale)
            
            canvas.drawBitmap(originalBitmap, matrix, null)
            
            // Save to temp file
            val tempFile = File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg")
            val out = FileOutputStream(tempFile)
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            
            originalBitmap.recycle()
            croppedBitmap.recycle()
            
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
