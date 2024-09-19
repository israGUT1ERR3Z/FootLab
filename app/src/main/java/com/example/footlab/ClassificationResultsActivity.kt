package com.example.footlab

import ClusterUtils
import ImageClassifier
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ClasificacionResultsActivity : AppCompatActivity() {

    private lateinit var classifier: ImageClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clasificacion_results)

        val originalImageView = findViewById<ImageView>(R.id.originalImageView)
        val tissue1ImageView = findViewById<ImageView>(R.id.tissue1ImageView)
        val tissue2ImageView = findViewById<ImageView>(R.id.tissue2ImageView)
        val tissue3ImageView = findViewById<ImageView>(R.id.tissue3ImageView)
        val resultTextView = findViewById<TextView>(R.id.classificationResultTextView)

        val imageUrl = intent.getStringExtra("IMAGE_URL")

        // Load clusters from assets
        val clusterUtils = ClusterUtils()
        val clustersJsonArray = clusterUtils.loadClustersFromAssets(this)
        val clusters = clusterUtils.processClusters(clustersJsonArray)
        classifier = ImageClassifier(clusters)

        imageUrl?.let { url ->
            lifecycleScope.launch {
                val bitmap = loadImageAsync(url)
                bitmap?.let {
                    val resizedBitmap = Bitmap.createScaledBitmap(it, 224, 224, true)
                    originalImageView.setImageBitmap(resizedBitmap)
                    val pixelValues = convertBitmapToPixelValues(resizedBitmap)
                    val classificationResults = withContext(Dispatchers.Default) {
                        classifier.classifyImage(pixelValues)
                    }
                    val tissueBitmaps = applyClassificationToBitmaps(resizedBitmap, classificationResults)

                    // Display the classified images in respective ImageViews
                    tissue1ImageView.setImageBitmap(tissueBitmaps.getOrNull(0))
                    tissue2ImageView.setImageBitmap(tissueBitmaps.getOrNull(1))
                    tissue3ImageView.setImageBitmap(tissueBitmaps.getOrNull(2))

                    // Display classification results
                    resultTextView.text = "Classification: ${classificationResults.joinToString()}"
                }
            }
        }
    }

    private suspend fun loadImageAsync(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val tempFile = File.createTempFile("temp", ".jpg", cacheDir)
                FileOutputStream(tempFile).use { fileOutputStream ->
                    inputStream.copyTo(fileOutputStream)
                }
                inputStream.close()
                decodeSampledBitmapFromFile(tempFile.absolutePath, 224, 224)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun decodeSampledBitmapFromFile(filepath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(filepath, this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }
        return BitmapFactory.decodeFile(filepath, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun convertBitmapToPixelValues(bitmap: Bitmap): Array<FloatArray> {
        val width = bitmap.width
        val height = bitmap.height
        val pixelValues = Array(width * height) { FloatArray(3) }  // One array for each pixel (RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val index = y * width + x
                pixelValues[index][0] = (pixel shr 16 and 0xFF) / 255f  // Red
                pixelValues[index][1] = (pixel shr 8 and 0xFF) / 255f   // Green
                pixelValues[index][2] = (pixel and 0xFF) / 255f         // Blue
            }
        }
        return pixelValues
    }

    private fun applyClassificationToBitmaps(bitmap: Bitmap, classificationResults: Array<Int>): Array<Bitmap> {
        val width = bitmap.width
        val height = bitmap.height
        val pixelCount = width * height

        val segmentedBitmaps = Array(3) { Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) }
        val colors = arrayOf(Color.RED, Color.GREEN, Color.BLUE)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixelIndex = y * width + x
                val tissueType = classificationResults.getOrNull(pixelIndex) ?: continue
                if (tissueType in 0..2) {
                    segmentedBitmaps[tissueType].setPixel(x, y, colors[tissueType])
                }
            }
        }

        return segmentedBitmaps
    }
}
