package com.example.footlab

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import org.tensorflow.lite.Interpreter
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class FotosAdapter(
    private val context: Context,
    private val fotosUrls: List<String>,
    private val interpreter: Interpreter
) : RecyclerView.Adapter<FotosAdapter.FotosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotosViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false)
        return FotosViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotosViewHolder, position: Int) {
        val url = fotosUrls[position]
        Glide.with(context)
            .load(url)
            .into(holder.imagenFotoItem)

        // Handle "Clasificar" button click


        // Handle "Segmentar" button click (for existing segmentation functionality)
        holder.botonSegmentarItem.setOnClickListener {
            thread {
                val bitmap = loadBitmapFromURL(url)
                bitmap?.let {
                    val targetSize = Pair(224, 224)
                    val (maskImage, segmentedImage) = predictMask(bitmap, targetSize)

                    saveBitmapAndGetUrl(maskImage) { maskImageUrl ->
                        saveBitmapAndGetUrl(segmentedImage) { segmentedImageUrl ->
                            val intent = Intent(context, ResultsActivity::class.java)
                            intent.putExtra("MASK_IMAGE_URL", maskImageUrl)
                            intent.putExtra("SEGMENTED_IMAGE_URL", segmentedImageUrl)
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = fotosUrls.size

    private fun loadBitmapFromURL(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            BitmapFactory.decodeStream(BufferedInputStream(connection.inputStream))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun predictMask(bitmap: Bitmap, targetSize: Pair<Int, Int>): Pair<Bitmap, Bitmap> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize.first, targetSize.second, true)

        val inputBuffer = ByteBuffer.allocateDirect(4 * targetSize.first * targetSize.second * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(targetSize.first * targetSize.second)
        resizedBitmap.getPixels(intValues, 0, targetSize.first, 0, 0, targetSize.first, targetSize.second)
        intValues.forEachIndexed { index, pixelValue ->
            inputBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        val outputBuffer = ByteBuffer.allocateDirect(4 * targetSize.first * targetSize.second)
        outputBuffer.order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, outputBuffer)

        val maskBitmap = Bitmap.createBitmap(targetSize.first, targetSize.second, Bitmap.Config.ARGB_8888)
        val segmentedBitmap = Bitmap.createBitmap(targetSize.first, targetSize.second, Bitmap.Config.ARGB_8888)
        outputBuffer.rewind()

        for (y in 0 until targetSize.second) {
            for (x in 0 until targetSize.first) {
                val value = outputBuffer.getFloat()
                val color = if (value > 0.5f) Color.WHITE else Color.BLACK
                maskBitmap.setPixel(x, y, color)
                segmentedBitmap.setPixel(x, y, if (color == Color.WHITE) resizedBitmap.getPixel(x, y) else Color.BLACK)
            }
        }

        return Pair(maskBitmap, segmentedBitmap)
    }

    private fun saveBitmapAndGetUrl(bitmap: Bitmap, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.png")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        callback(uri.toString())
                    }
                    .addOnFailureListener {
                        callback("") // Handle URL download error
                    }
            }
            .addOnFailureListener {
                callback("") // Handle image upload error
            }
    }

    class FotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenFotoItem: ImageView = itemView.findViewById(R.id.imagenFotoItem)
        val botonClasificarItem: Button = itemView.findViewById(R.id.botonClasificarItem)
        val botonSegmentarItem: Button = itemView.findViewById(R.id.botonSegmentarItem)
    }
}
