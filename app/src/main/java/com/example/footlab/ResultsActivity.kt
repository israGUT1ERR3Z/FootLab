package com.example.footlab

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val maskImageView = findViewById<ImageView>(R.id.maskImageView)
        val segmentedImageView = findViewById<ImageView>(R.id.segmentedImageView)

        val maskImageUrl = intent.getStringExtra("MASK_IMAGE_URL")
        val segmentedImageUrl = intent.getStringExtra("SEGMENTED_IMAGE_URL")

        // Load images asynchronously
        maskImageUrl?.let { url ->
            loadImageAsync(url) { bitmap ->
                bitmap?.let { maskBitmap ->
                    maskImageView.setImageBitmap(maskBitmap)
                }
            }
        }

        segmentedImageUrl?.let { url ->
            loadImageAsync(url) { bitmap ->
                bitmap?.let { segmentedBitmap ->
                    segmentedImageView.setImageBitmap(segmentedBitmap)
                }
            }
        }
    }

    private fun loadImageAsync(url: String, callback: (Bitmap?) -> Unit) {
        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()  // Close InputStream after use
                runOnUiThread { callback(bitmap) }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { callback(null) }
            }
        }
    }
}
