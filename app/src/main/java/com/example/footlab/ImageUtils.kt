package com.example.footlab.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // Load image from filePath
    fun loadImage(filePath: String): Bitmap? {
        return try {
            // Load and resize the image to 224x224
            val originalBitmap = BitmapFactory.decodeFile(filePath)
            Bitmap.createScaledBitmap(originalBitmap, 224, 224, true)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // Save image to filePath
    fun saveImage(image: Bitmap, filePath: String, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG) {
        try {
            val file = File(filePath)
            val outputStream = FileOutputStream(file)
            image.compress(format, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Get the color of a pixel in the image
    fun getPixelColor(image: Bitmap, x: Int, y: Int): Int {
        return image.getPixel(x, y)
    }

    // Set the color of a pixel in the image
    fun setPixelColor(image: Bitmap, x: Int, y: Int, color: Int) {
        image.setPixel(x, y, color)
    }

    // Convert Bitmap to an array of pixel values (RGB)
    fun bitmapToPixelArray(bitmap: Bitmap): Array<FloatArray> {
        val width = bitmap.width
        val height = bitmap.height
        val pixelArray = Array(height) { FloatArray(width * 3) }  // Assuming RGB values

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16 and 0xff) / 255f
                val g = (pixel shr 8 and 0xff) / 255f
                val b = (pixel and 0xff) / 255f
                pixelArray[y][x * 3] = r
                pixelArray[y][x * 3 + 1] = g
                pixelArray[y][x * 3 + 2] = b
            }
        }
        return pixelArray
    }
}
